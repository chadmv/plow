package com.breakersoft.plow.scheduler;

import java.util.List;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.rnd.thrift.RunningTask;

public interface StatsService {

    void updateProcRuntimeStats(List<RunningTask> tasks);

    void updateTaskRuntimeStats(List<RunningTask> tasks);

    void recalculateLayerMinimumRam(Layer layer);

    void recalculateLayerMinimumCores(Layer layer);

    void updateLayerRuntimeStats(List<RunningTask> tasks);

}
