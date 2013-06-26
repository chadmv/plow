package com.breakersoft.plow.dispatcher.dao;

import java.util.List;

import com.breakersoft.plow.rnd.thrift.RunningTask;

public interface StatsDao {

    void batchUpdateProcRuntimeStats(List<RunningTask> tasks);

    void batchUpdateTaskRuntimeStats(List<RunningTask> tasks);

    void batchUpdateJobRuntimeStats(List<RunningTask> tasks);

    void batchUpdateLayerRuntimeStats(List<RunningTask> tasks);

    void batchUpdateLayerMinimumMemory(List<RunningTask> tasks);

}
