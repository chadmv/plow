package com.breakersoft.plow.dispatcher;

import java.util.List;

import com.breakersoft.plow.rnd.thrift.RunningTask;

public interface StatsService {

    void updateProcRuntimeStats(List<RunningTask> tasks);

    void updateTaskRuntimeStats(List<RunningTask> tasks);

    void updateLayerRuntimeStats(List<RunningTask> tasks);

    void updateJobRuntimeStats(List<RunningTask> tasks);

    void recalculateLayerMinimumMemory(List<RunningTask> tasks);
}
