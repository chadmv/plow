package com.breakersoft.plow.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FrameSet;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.exceptions.InvalidBlueprintException;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskSpecT;
import com.breakersoft.plow.thrift.TaskState;


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
    FolderDao folderDao;

    @Autowired
    EventManager eventManager;

    @Autowired
    DependService dependService;

    @Override
    public JobLaunchEvent launch(JobSpecT jobspec) {

        logger.info("launching job spec: {} ", jobspec);

        final Project project = projectDao.get(jobspec.getProject());
        final Job job = jobDao.create(project, jobspec);
        final Folder folder = filterJob(job, project);

        createJobTopology(job, project, jobspec);
        createDependencies(job, jobspec);

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);
        jobDao.setJobState(job, JobState.RUNNING);

        //TODO: do this someplace else. (tranny hook or aspect)
        // Don't want to add jobs to the dispatcher that fail to
        // commit to the DB.
        JobLaunchEvent event = new JobLaunchEvent(job, folder, jobspec);
        eventManager.post(event);

        logger.info("Job {} launch success", job.getName());

        return event;
    }

    @Override
    public boolean shutdown(Job job) {
        if (jobDao.shutdown(job)) {
            return true;
        }
        return false;
    }

    private Folder filterJob(Job job, Project project) {
        // TODO: fully implement
        Folder folder = folderDao.getDefaultFolder(project);
        jobDao.updateFolder(job, folder);
        return folder;
    }

    private void createJobTopology(
            Job job, Project project, JobSpecT jobspec) {

        int layerOrder = 0;
        for (LayerSpecT blayer: jobspec.getLayers()) {
            Layer layer = layerDao.create(job, blayer, layerOrder);

            if (blayer.isSetRange()) {
                logger.info("Creating layer {}, range: {}", blayer.name, blayer.range);
                int frameOrder = 0;
                FrameSet frameSet = new FrameSet(blayer.getRange());
                for (int frameNum: frameSet) {
                    taskDao.create(layer, String.format("%04d-%s", frameNum, blayer.getName()),
                            frameNum, frameOrder, layerOrder);
                    frameOrder++;
                }
            }
            else if (blayer.isSetTasks()) {
                logger.info("Creating tasks in layer: {}", blayer.name);
                int taskOrder = 0;
                for (TaskSpecT task: blayer.getTasks()) {
                    logger.info("Creating task: {}", task.getName());
                    taskDao.create(layer, task.getName(), 0, taskOrder, layerOrder);
                }
            }
            else {
                throw new InvalidBlueprintException(
                        "Layer {} cannot be launched, has no range or tasks.");
            }
            layerOrder++;
        }
    }

    private void createDependencies(Job job, JobSpecT jspec) {

        logger.info("Setting up dependencies in job {}", jspec.name);

        for (LayerSpecT layer: jspec.getLayers()) {
            if (!layer.isSetDepends()) {
                continue;
            }
            for (DependSpecT depend: layer.getDepends()) {
                dependService.createDepend(job, depend);
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
    public void setJobAttrs(Job job, Map<String,String> attrs) {
        jobDao.setAttrs(job, attrs);
    }

    @Override
    public boolean hasWaitingFrames(Job job) {
        return jobDao.hasWaitingFrames(job);
    }

    @Override
    public boolean isFinished(Job job) {
        return jobDao.isFinished(job);
    }

    @Override
    public Task getTask(String id) {
        return taskDao.get(UUID.fromString(id));
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
    public boolean isJobPaused(Job job) {
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
    public void updateMaxRssValues(List<RunningTask> runningTasks) {
        Collections.sort(runningTasks, new Comparator<RunningTask>() {
            @Override
            public int compare(RunningTask o1, RunningTask o2) {
                return o1.layerId.compareTo(o2.layerId);
            }
        });

        for (RunningTask task: runningTasks) {
            layerDao.updateMaxRssMb(UUID.fromString(task.layerId), task.rssMb);
            layerDao.updateMaxCpuPerc(UUID.fromString(task.layerId), task.cpuPercent);
        }

        for (RunningTask task: runningTasks) {
            jobDao.updateMaxRssMb(UUID.fromString(task.jobId), task.rssMb);
        }

    }

    @Override
    public void updateRunningTasks(List<RunningTask> runningTasks) {
        // Sort the tasks by ID to ensure predicatable update.
        Collections.sort(runningTasks, new Comparator<RunningTask>() {
            @Override
            public int compare(RunningTask o1, RunningTask o2) {
                return o1.taskId.compareTo(o2.taskId);
            }
        });
        for (RunningTask task: runningTasks) {
            taskDao.updateTaskDispatchData(task);
        }
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
    public void setLayerTags(Layer layer, Set<String> tags) {
        layerDao.setTags(layer, tags);
    }

    @Override
    public void setLayerThreadable(Layer layer, boolean threadable) {
        layerDao.setThreadable(layer, threadable);
    }
}
