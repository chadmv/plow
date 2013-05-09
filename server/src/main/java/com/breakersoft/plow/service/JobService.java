package com.breakersoft.plow.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;

public interface JobService {

    /*
     * Jobs
     */

    /**
     * Launch a job spec.
     *
     * @param jobspec
     * @return
     */
    JobLaunchEvent launch(JobSpecT jobspec);

    /**
     * Shut down a job.
     *
     * @param job
     * @return
     */
    boolean shutdown(Job job);

    /**
     * Set the maximum core value on a given job.  If a job reaches its maximum
     * cores it will no longer be dispatched new cores. If a job is well over
     * its maximum cores, then procs will fall off the job until its at or under
     * its maximum cores value.
     *
     * @param job
     * @param value
     */
    void setJobMaxCores(Job job, int value);

    /**
     * Set the minumum core value on a given job.  The minimum cores value controls
     * the overall priority of a job relative to other jobs.  Jobs with a higher
     * minumum cores get more procs.
     *
     * @param job
     * @param value
     */
    void setJobMinCores(Job job, int value);

    /**
     * Get a job using its unique UUID.
     * @param id
     * @return
     */
    Job getJob(UUID id);

    /**
     * Return true if a job has waiting frames.
     *
     * @param job
     * @return
     */
    boolean hasWaitingFrames(Job job);

    /**
     * Set the state of a job and return true if the state
     * actually changed.
     *
     * @param job
     * @param state
     * @return
     */
    boolean setJobState(Job job, JobState state);

    /**
     * Set job paused or unpaused.
     *
     * @param job
     * @param value
     */
    void setJobPaused(Job job, boolean value);

    /**
     * Return true if the job is paused.
     *
     * @param job
     * @return
     */
    boolean isJobPaused(Job job);

    /**
     * Set the arbitrary attrs on a job.
     *
     * @param job
     * @param attrs
     */
    void setJobAttrs(Job job, Map<String, String> attrs);

    /*
     * Layers
     */

    void setLayerMinCores(Layer layer, int cores);
    void setLayerMaxCores(Layer layer, int cores);
    void setLayerMinRam(Layer layer, int ram);
    void setLayerTags(Layer layer, Set<String> tags);
    void setLayerThreadable(Layer layer, boolean threadable);
    Layer getLayer(Job job, String layer);
    Layer getLayer(Job job, int idx);
    boolean isLayerComplete(Layer layer);
    Layer getLayer(UUID id);
    void addLayerOutput(Layer layer, String path, Map<String, String> attrs);

    /*
     * Tasks
     */



    Task getTask(UUID id);
    boolean isFinished(Job job);
    Task getTask(Layer layer, int number);
    List<Task> getTasks(TaskFilterT filter);
    boolean setTaskState(Task task, TaskState currentState, TaskState newState);
    boolean setTaskState(Task task, TaskState state);

}
