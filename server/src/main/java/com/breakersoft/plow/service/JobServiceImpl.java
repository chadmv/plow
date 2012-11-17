package com.breakersoft.plow.service;

import java.util.Map;
import java.util.UUID;

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
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.TaskState;


@Service
@Transactional
public class JobServiceImpl implements JobService {

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

            int frameOrder = 0;
            FrameSet frameSet = new FrameSet(blayer.getRange());
            for (int frameNum: frameSet) {
                taskDao.create(layer, String.format("%04d-%s", frameNum, blayer.getName()),
                        frameNum, frameOrder, layerOrder);
                frameOrder++;
            }
            layerOrder++;
        }
    }

    private void createDependencies(Job job, JobSpecT jspec) {

        for (LayerSpecT layer: jspec.getLayers()) {
            if (!layer.isSetDepends()) {
                continue;
            }
            for (DependSpecT depend: layer.getDepends()) {
                // Fill in job IDs
                if (!depend.isSetDependentJob()) {
                    depend.setDependentJob(job.getJobId().toString());
                }

                if (!depend.isSetDependOnJob()) {
                    depend.setDependOnJob(job.getJobId().toString());
                }

                dependService.createDepend(depend);
            }
        }

        if (jspec.isSetDepends()) {
            for (DependSpecT depend: jspec.getDepends()) {

                if (!depend.isSetDependentJob()) {
                    depend.setDependentJob(job.getJobId().toString());
                }

                if (!depend.isSetDependOnJob()) {
                    depend.setDependOnJob(job.getJobId().toString());
                }

                dependService.createDepend(depend);
            }
        }
    }


    @Override
    public Job getJob(String id) {
        return jobDao.get(UUID.fromString(id));
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
}
