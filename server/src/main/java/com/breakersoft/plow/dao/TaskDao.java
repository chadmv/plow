package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.thrift.TaskState;

public interface TaskDao {

    Task create(Layer layer, int number, int frameOrder, int layerOrder);

    Task get(Layer layer, int number);

    Task get(UUID id);

    boolean updateState(Task task, TaskState currentState,
            TaskState newState);
}
