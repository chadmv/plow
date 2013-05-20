package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;

public interface TaskDao {

    Task create(Layer layer, String name, int number, int frameOrder, int layerOrder, int minRam);

    Task get(Layer layer, int number);

    Task get(UUID id);

    boolean updateState(Task task, TaskState currentState, TaskState newState);

    Task getByNameOrId(Job job, String identifer);

    void clearLastLogLine(Task task);

    List<Task> getTasks(TaskFilterT filter);

    boolean setTaskState(Task task, TaskState newState);

    boolean setTaskState(Task task, TaskState newState, TaskState oldState);

    void batchCreate(Layer layer, String range, int layerOrder, int minRam);


}
