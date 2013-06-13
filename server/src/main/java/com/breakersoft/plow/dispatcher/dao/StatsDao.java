package com.breakersoft.plow.dispatcher.dao;

import com.breakersoft.plow.rnd.thrift.RunningTask;

public interface StatsDao {

    boolean updateProcRuntimeStats(RunningTask task);

    boolean updateTaskRuntimeStats(RunningTask task);

    void updateLayerRuntimeStats(RunningTask task);

}
