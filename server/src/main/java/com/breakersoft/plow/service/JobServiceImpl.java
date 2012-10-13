package com.breakersoft.plow.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
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
}
