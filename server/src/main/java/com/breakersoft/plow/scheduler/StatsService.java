package com.breakersoft.plow.scheduler;

import java.util.List;

import com.breakersoft.plow.rnd.thrift.RunningTask;

public interface StatsService {

    void updateProcRuntimeStats(List<RunningTask> tasks);

}
