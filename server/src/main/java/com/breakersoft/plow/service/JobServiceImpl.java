package com.breakersoft.plow.service;

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
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.LayerBp;
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

    @Override
    public JobLaunchEvent launch(Blueprint blueprint) {

        final Project project = projectDao.get(blueprint.job.getProject());
        final Job job = jobDao.create(project, blueprint);
        final Folder folder = filterJob(job, project);

        createJobTopology(job, project, blueprint);

        jobDao.updateFrameStatesForLaunch(job);
        jobDao.updateFrameCountsForLaunch(job);
        jobDao.setJobState(job, JobState.RUNNING);

        //TODO: do this someplace else. (tranny hook or aspect)
        // Don't want to add jobs to the dispatcher that fail to
        // commit to the DB.
        JobLaunchEvent event = new JobLaunchEvent(job, folder, blueprint);
        eventManager.post(event);

        return event;
    }

    @Override
    public boolean shutdown(Job job) {
        if (jobDao.shutdown(job)) {
            eventManager.post(new JobFinishedEvent(job));
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
            Job job, Project project, Blueprint blueprint) {

        int layerOrder = 0;
        for (LayerBp blayer: blueprint.getLayers()) {
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

    @Override
    public Job getJob(String id) {
        return jobDao.get(UUID.fromString(id));
    }

    @Override
    public boolean hasWaitingFrames(Job job) {
        return jobDao.hasWaitingFrames(job);
    }

    @Override
    public boolean hasPendingFrames(Job job) {
        return jobDao.hasPendingFrames(job);
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
    public boolean startTask(Task task, DispatchProc proc) {
        return taskDao.start(task);
    }

    @Override
    public boolean stopTask(Task task, TaskState state) {
        return taskDao.stop(task, state);
    }

    @Override
    public boolean reserveTask(Task task) {
        return taskDao.reserve(task);
    }

    @Override
    public boolean unreserveTask(Task task) {
        return taskDao.unreserve(task);
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
    public boolean setJobState(Job job, JobState state) {
        return jobDao.setJobState(job, state);
    }
}
