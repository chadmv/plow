package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.thrift.TaskState;

public interface TaskDao {

    Task create(Layer layer, String name, int number, int frameOrder, int layerOrder);

    Task get(Layer layer, int number);

    Task get(UUID id);

    boolean updateState(Task task, TaskState currentState,
            TaskState newState);

    boolean reserve(Task frame);

    boolean unreserve(Task frame);

    boolean start(Task task);

    boolean stop(Task task, TaskState newState);

}
