package com.breakersoft.plow.scheduler.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;

@Repository
public class StatsDaoImpl extends AbstractDao implements StatsDao {


    private static final String UPDATE_RUNTIME =
            "UPDATE " +
                "plow.proc " +
            "SET " +
                "int_ram_used=?,"+
                "int_ram_high=?,"+
                "int_progress=?,"+
                "flt_cores_used=?,"+
                "flt_cores_high=?,"+
                "str_last_log_line=? " +
            "WHERE " +
                "pk_proc=? " +
            "AND " +
                "pk_task=?";

    @Override
    public boolean updateRuntimeStats(RunningTask task) {
        float cores_used = task.cpuPercent / 100.0f;
        return jdbc.update(UPDATE_RUNTIME, task.rssMb, task.rssMb, task.progress, cores_used, cores_used, task.lastLog,
                UUID.fromString(task.procId), UUID.fromString(task.taskId)) == 1;
    }
}
