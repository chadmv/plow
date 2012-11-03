package com.breakersoft.plow.service;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.TaskState;

public interface JobService {

    Task getTask(String id);

    boolean setTaskState(Task task, TaskState currentState, TaskState newState);

    boolean hasWaitingFrames(Job job);

    Job getJob(String id);

    boolean hasPendingFrames(Job job);

    Task getTask(Layer layer, int number);

    Layer getLayer(Job job, String layer);

    boolean setJobState(Job job, JobState state);

    JobLaunchEvent launch(JobSpecT jobspec);

    boolean shutdown(Job job);
}
