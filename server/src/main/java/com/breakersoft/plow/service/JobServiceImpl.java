package com.breakersoft.plow.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.FilterableJob;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FrameRange;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.ServiceFull;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.dao.ServiceDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.exceptions.JobSpecException;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskSpecT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.util.PlowUtils;


@Service
@Transactional
public class JobServiceImpl implements JobService {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    JobDao jobDao;

    @Autowired
    LayerDao layerDao;

    @Autowired
    TaskDao taskDao;

    @Autowired
    ProjectDao projectDao;

    @Autowired
    ServiceDao serviceDao;

    @Autowired
    FolderDao folderDao;

    @Autowired
    EventManager eventManager;

    @Autowired
    DependService dependService;

    @Autowired
    FilterService filterService;

    @Override
    public JobLaunchEvent launch(JobSpecT jobspec) {

        logger.info("launching job spec: {} ", jobspec);

        final Project project = projectDao.get(jobspec.getProject());
        final FilterableJob job = jobDao.create(project, jobspec, false);
        final Folder folder = filterJob(job, project);

        createJobTopology(job, project, jobspec, false);
        createDependencies(job, jobspec, false);

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);
        jobDao.setJobState(job, JobState.RUNNING);

        createPostJob(job, jobspec);

        //TODO: do this someplace else. (tranny hook or aspect)
        // Don't want to add jobs to the dispatcher that fail to
        // commit to the DB.
        JobLaunchEvent event = new JobLaunchEvent(job, folder, jobspec);
        eventManager.post(event);

        logger.info("{} launch success", job.getName());
        return event;
    }

    private void createPostJob(Job parentJob, JobSpecT jobspec) {

        boolean hasPostLayers = false;
        // check if we have post layers!
        for (LayerSpecT blayer: jobspec.getLayers()) {
            if (blayer.isPost) {
                hasPostLayers = true;
                break;
            }
        }

        if (!hasPostLayers) {
            return;
        }

        final FilterableJob job = jobDao.create(parentJob, jobspec, true);
        filterJob(job, parentJob);

        createJobTopology(job, parentJob, jobspec, true);
        createDependencies(job, jobspec, true);

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);
        jobDao.setJobState(job, JobState.POST);
        jobDao.tiePostJob(parentJob, job);
    }

    @Override
    public boolean shutdown(Job job) {
        if (jobDao.shutdown(job)) {
            jobDao.flipPostJob(job);
            return true;
        }
        return false;
    }

    private Folder filterJob(FilterableJob job, Project project) {
        // TODO: fully implement

        // First put it in the default folder.
        Folder folder = folderDao.getDefaultFolder(project);
        jobDao.updateFolder(job, folder);

        List<MatcherFull> matchers = filterService.getMatchers(job);
        filterService.filterJob(matchers, job);

        return folder;
    }

    private void createJobTopology(
            Job job, Project project, JobSpecT jobspec, boolean postJob) {

        int layerOrder = 0;
        for (LayerSpecT blayer: jobspec.getLayers()) {

            if (blayer.isPost != postJob) {
                continue;
            }

            PlowUtils.alpahNumCheck(blayer.name, "The layer name must be alpha numeric:" + blayer.getName());

            if (blayer.isSetRange()) {
                logger.info("Creating layer {}, range: {}", blayer.name, blayer.range);

                final FrameRange frameRange = new FrameRange(blayer.range, blayer.chunk);
                prepLayer(blayer, frameRange);

                final Layer layer = layerDao.create(job, blayer, layerOrder);
                taskDao.batchCreate(layer, frameRange, layerOrder, blayer.minRam);
            }
            else if (blayer.isSetTasks()) {
                logger.info("Creating tasks in layer: {}", blayer.name);

                prepLayer(blayer, null);
                final Layer layer = layerDao.create(job, blayer, layerOrder);

                int taskOrder = 0;
                for (TaskSpecT task: blayer.getTasks()) {
                    PlowUtils.alpahNumCheck(task.getName(), "Task name must be alpha numeric: " + task.getName());
                    taskDao.create(layer, task.getName(), 0, taskOrder, layerOrder, blayer.minRam);
                }
            }
            else {
                throw new JobSpecException(
                        "Layer {} cannot be launched, has no range or tasks.");
            }
            layerOrder++;
        }
    }

    /**
     * Apply defaults for unset layer values.
     *
     * @param layer
     */
    private void prepLayer(LayerSpecT layer, FrameRange frameRange) {

        // Set a default service name, although it might not be setup
        // as a valid service.
        if (!PlowUtils.isValid(layer.getServ())) {
            logger.info("Setting server on {} to {}", layer.name, Defaults.DEFAULT_SERVICE);
            layer.setServ(Defaults.DEFAULT_SERVICE);
        }

        // Check if there is a service and apply values
        ServiceFull service = serviceDao.getServiceFull(layer.getServ());
        if (service != null) {

            if (service.isSetMinCores() && !layer.isSetMinCores()) {
                layer.setMinCores(service.getMinCores());
            }

            if (service.isSetMaxCores() && !layer.isSetMaxCores()) {
                layer.setMaxCores(service.getMaxCores());
            }

            if (service.isSetMaxRam() && !layer.isSetMaxRam()) {
                layer.setMaxRam(service.getMaxRam());
            }

            if (service.isSetMinRam() && !layer.isSetMinRam()) {
                layer.setMinRam(service.getMinRam());
            }

            if (service.isSetMaxRetries() && !layer.isSetMaxRetries()) {
                layer.setMaxRetries(service.getMaxRetries());
            }

            if (service.isSetTags() && !layer.isSetTags()) {
                layer.setTags(service.getTags());
            }

            if (service.isSetThreadable() && !layer.isSetThreadable()) {
                layer.setThreadable(service.isThreadable());
            }
        }

        if (!layer.isSetMaxCores()) {
            logger.info("Setting max cores default on {} to {}", layer.name, Defaults.DEFAULT_MAX_CORES);
            layer.setMaxCores(Defaults.DEFAULT_MAX_CORES);
        }

        if (!layer.isSetMinCores()) {
            logger.info("Setting min cores default on {} to {}", layer.name, Defaults.DEFAULT_MIN_CORES);
            layer.setMinCores(Defaults.DEFAULT_MIN_CORES);
        }

        if (!layer.isSetMinRam()) {
            logger.info("Setting min ram default on {} to {}", layer.name, Defaults.DEFAULT_MIN_RAM);
            layer.setMinRam(Defaults.DEFAULT_MIN_RAM);
        }

        if (!layer.isSetMaxRam()) {
            logger.info("Setting max ram default on {} to {}", layer.name, Defaults.DEFAULT_MAX_RAM);
            layer.setMaxRam(Defaults.DEFAULT_MAX_RAM);
        }

        if (!layer.isSetMaxRetries()) {
            logger.info("Setting max retries default on {} to {}", layer.name, Defaults.DEFAULT_MAX_RETRIES);
            layer.setMaxRetries(Defaults.DEFAULT_MAX_RETRIES);
        }

        if (!layer.isSetThreadable()) {
            logger.info("Setting threadable default on {} to {}", layer.name, Defaults.DEFAULT_THREADABLE);
            layer.setThreadable(Defaults.DEFAULT_THREADABLE);
        }

        if (!layer.isSetTags()) {
            logger.info("Setting tags default on {} to {}", layer.name, Defaults.DEFAULT_TAGS);
            layer.setTags(Defaults.DEFAULT_TAGS);
        }

        if (frameRange != null) {
            if (layer.chunk <= 0) {
                logger.info("Setting chunk size to %d", frameRange.chunkSize);
                layer.chunk = frameRange.chunkSize;
            }
        }
    }

    private void createDependencies(Job job, JobSpecT jspec, boolean postJob) {

        logger.info("Setting up dependencies in job {}", jspec.name);

        for (LayerSpecT layer: jspec.getLayers()) {

            if (layer.isPost != postJob) {
                continue;
            }

            if (layer.isSetDepends()) {
                for (DependSpecT depend: layer.getDepends()) {
                    dependService.createDepend(job, depend);
                }
            }

            if (layer.isSetTasks()) {
                for (TaskSpecT task: layer.getTasks())  {
                    if (task.isSetDepends()) {
                        for (DependSpecT depend: task.getDepends()) {
                            dependService.createDepend(job, depend);
                        }
                    }
                }
            }
        }

        if (jspec.isSetDepends()) {
            for (DependSpecT depend: jspec.getDepends()) {
                dependService.createDepend(job, depend);
            }
        }
    }

    @Override
    public void setJobMinCores(Job job, int value) {
        jobDao.setMinCores(job, value);
    }

    @Override
    public void setJobMaxCores(Job job, int value) {
        jobDao.setMaxCores(job, value);
    }

    @Override
    public Job getJob(UUID id) {
        return jobDao.get(id);
    }

    @Override
    public Job getActiveJob(UUID id) {
        return jobDao.getActive(id);
    }

    @Override
    public void setJobAttrs(Job job, Map<String,String> attrs) {
        jobDao.setAttrs(job, attrs);
    }

    @Override
    public boolean hasWaitingFrames(Job job) {
        return jobDao.hasWaitingFrames(job);
    }

    @Override
    public boolean isFinished(JobId job) {
        return jobDao.isFinished(job);
    }

    @Override
    public Task getTask(UUID id) {
        return taskDao.get(id);
    }

    @Override
    public boolean setTaskState(Task task, TaskState currentState, TaskState newState) {
        return taskDao.updateState(task, currentState, newState);
    }

    @Override
    public Task getTask(Layer layer, int number) {
        return taskDao.get(layer, number);
    }

    @Override
    public Layer getLayer(Job job, String layer) {
        return layerDao.get(job, layer);
    }

    @Override
    public Layer getLayer(Job job, int idx) {
        return layerDao.get(job, idx);
    }

    @Override
    public Layer getLayer(UUID id) {
        return layerDao.get(id);
    }

    @Override
    public void addLayerOutput(Layer layer, String path, Map<String,String> attrs) {
        layerDao.addOutput(layer, path, attrs);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean isLayerComplete(Layer layer) {
        return layerDao.isFinished(layer);
    }

    @Override
    public boolean setJobState(Job job, JobState state) {
        return jobDao.setJobState(job, state);
    }

    @Override
    public void setJobPaused(Job job, boolean value) {
        jobDao.setPaused(job, value);
    }

    @Override
    public boolean isJobPaused(JobId job) {
        return jobDao.isPaused(job);
    }

    @Override
    @Transactional(readOnly=true)
    public List<Task> getTasks(TaskFilterT filter) {
        return taskDao.getTasks(filter);
    }

    @Override
    public boolean setTaskState(Task task, TaskState state) {
        return taskDao.setTaskState(task, state);
    }

    @Override
    public void setLayerMinCores(Layer layer, int cores) {
        layerDao.setMinCores(layer, cores);
    }

    @Override
    public void setLayerMaxCores(Layer layer, int cores) {
        layerDao.setMaxCores(layer, cores);
    }

    @Override
    public void setLayerMinRam(Layer layer, int ram) {
        layerDao.setMinRam(layer, ram);
    }

    @Override
    public void setLayerTags(Layer layer, List<String> tags) {
        layerDao.setTags(layer, tags);
    }

    @Override
    public void setLayerThreadable(Layer layer, boolean threadable) {
        layerDao.setThreadable(layer, threadable);
    }
}
