package com.breakersoft.plow.scheduler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.scheduler.dao.StatsDao;

@Service
@Transactional
public class StatsServiceImpl implements StatsService {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(StatsServiceImpl.class);

    @Autowired
    StatsDao statsDao;

    @Override
    public void updateProcRuntimeStats(List<RunningTask> tasks) {

        // Sort to ensure predictable update order.
        Collections.sort(tasks, new Comparator<RunningTask>() {
            @Override
            public int compare(RunningTask o1, RunningTask o2) {
                return o1.procId.compareTo(o2.procId);
            }
        });
        for (RunningTask task: tasks) {
            logger.info("Updating stats {}", task);
            statsDao.updateRuntimeStats(task);
        }
    }
}
