package com.breakersoft.plow.scheduler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Layer;
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

    private static final Comparator<RunningTask> SORT_BY_PROC = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.procId.compareTo(o2.procId);
        }
    };

    private static final Comparator<RunningTask> SORT_BY_TASK = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.taskId.compareTo(o2.taskId);
        }
    };

    @Override
    public void updateProcRuntimeStats(List<RunningTask> tasks) {

        // Sort to ensure predictable update order.
        Collections.sort(tasks, SORT_BY_PROC);

        for (RunningTask task: tasks) {
            statsDao.updateProcRuntimeStats(task);
        }
    }

    @Override
    public void updateTaskRuntimeStats(List<RunningTask> tasks) {

        // Sort to ensure predictable update order.
        Collections.sort(tasks, SORT_BY_TASK);

        for (RunningTask task: tasks) {
            statsDao.updateTaskRuntimeStats(task);
        }
    }

    @Override
    public void updateLayerRuntimeStats(List<RunningTask> tasks) {


    }

    @Override
    public void recalculateLayerMinimumRam(Layer layer) {

    }

    @Override
    public void recalculateLayerMinimumCores(Layer layer) {

    }
}
