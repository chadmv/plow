package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;

public interface DependDao {

    public Depend createJobOnJob(Job dependent, Job dependOn);
    public Depend createLayerOnLayer(Layer dependent, Layer dependOn);
    public Depend createLayerOnTask(Layer dependent, Task dependOn);
    public Depend createTaskOnLayer(Task dependent, Layer dependOn);
    public Depend createTaskOnTask(Task dependent, Task dependOn);
    void incrementDependCounts(Depend depend);
    void decrementDependCounts(Depend depend);
    Depend get(UUID id);

    List<Depend> getOnTaskDepends(Task task);
    boolean satisfyDepend(Depend depend);
}
