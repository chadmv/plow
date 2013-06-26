package com.breakersoft.plow.dispatcher;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dispatcher.dao.StatsDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;

/**
 * Service for updating/maintaining statistics.
 *
 */
@Service
@Transactional
public class StatsServiceImpl implements StatsService {

    @Autowired
    StatsDao statsDao;

    @Override
    public void updateProcRuntimeStats(List<RunningTask> tasks) {
        statsDao.batchUpdateProcRuntimeStats(tasks);
    }

    @Override
    public void updateTaskRuntimeStats(List<RunningTask> tasks) {
        statsDao.batchUpdateTaskRuntimeStats(tasks);
    }

    @Override
    public void updateLayerRuntimeStats(List<RunningTask> tasks) {
        statsDao.batchUpdateLayerRuntimeStats(tasks);
    }

    @Override
    public void updateJobRuntimeStats(List<RunningTask> tasks) {
        statsDao.batchUpdateJobRuntimeStats(tasks);
    }

    @Override
    public void recalculateLayerMinimumMemory(List<RunningTask> tasks) {
        statsDao.batchUpdateLayerMinimumMemory(tasks);
    }
}
