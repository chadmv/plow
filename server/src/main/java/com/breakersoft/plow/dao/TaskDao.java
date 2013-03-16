package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;

public interface TaskDao {

    Task create(Layer layer, String name, int number, int frameOrder, int layerOrder);

    Task get(Layer layer, int number);

    Task get(UUID id);

    boolean updateState(Task task, TaskState currentState,
            TaskState newState);

    boolean reserve(Task frame);

    boolean unreserve(Task frame);

    boolean stop(Task task, TaskState newState);

    Task getByNameOrId(Job job, String identifer);

    void updateTaskDispatchData(RunningTask runTask);

    void clearLastLogLine(Task task);

    boolean start(Task task, int cores, int memory);

    void resetTaskDispatchData(Task task, String host);

	List<Task> getTasks(TaskFilterT filter);

	boolean setTaskState(Task task, TaskState newState);

	boolean setTaskState(Task task, TaskState newState, TaskState oldState);

}
