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

    Task getTask(String id);


    boolean hasWaitingFrames(Job job);

    Job getJob(String id);

    boolean isFinished(Job job);

    Task getTask(Layer layer, int number);

    Layer getLayer(Job job, String layer);

    Layer getLayer(Job job, int idx);

    boolean setJobState(Job job, JobState state);

    JobLaunchEvent launch(JobSpecT jobspec);

    boolean shutdown(Job job);

    boolean isLayerComplete(Layer layer);

    void setJobPaused(Job job, boolean value);

    boolean isJobPaused(Job job);

    Layer getLayer(UUID id);

    void addLayerOutput(Layer layer, String path, Map<String, String> attrs);

    void updateRunningTasks(List<RunningTask> runningTasks);

    void updateMaxRssValues(List<RunningTask> runningTasks);

	List<Task> getTasks(TaskFilterT filter);

    boolean setTaskState(Task task, TaskState currentState, TaskState newState);

	boolean setTaskState(Task task, TaskState state);

	/*
	 * Layers
	 */
	void setLayerMinCores(Layer layer, int cores);
	void setLayerMaxCores(Layer layer, int cores);
	void setLayerMinRam(Layer layer, int ram);
	void setLayerTags(Layer layer, Set<String> tags);
	void setLayerThreadable(Layer layer, boolean threadable);
}
