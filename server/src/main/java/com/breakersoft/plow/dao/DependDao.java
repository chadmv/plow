package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;

public interface DependDao {

    public Depend createJobOnJob(Job dependent, Job dependOn);

    public Depend createLayerOnLayer(Job dependentJob, Layer dependent, Job dependOnJob, Layer dependOn);

    public Depend createLayerOnTask(Job dependentJob, Layer dependent, Job dependOnJob, Layer dependOnLayer, Task dependOn);

    public Depend createTaskOnLayer(
            Job dependentJob,
            Layer dependentLayer,
            Task dependentTask,
            Job dependOnJob,
            Layer dependOnLayer);

    public Depend createTaskOnTask(
            Job dependentJob,
            Layer dependentLayer,
            Task dependentTask,
            Job dependOnJob,
            Layer dependOnLayer,
            Task dependOnTask);

    void incrementDependCounts(Depend depend);
    void decrementDependCounts(Depend depend);

    Depend get(UUID id);
    boolean satisfyDepend(Depend depend);
    boolean unsatisfyDepend(Depend depend);

    List<Depend> getOnTaskDepends(Task task);
    List<Depend> getOnLayerDepends(Layer layer);
    List<Depend> getOnJobDepends(Job job);
}
