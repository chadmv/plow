package com.breakersoft.plow.scheduler.dao;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.TaskState;

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
                "str_last_log_line=?, " +
                "time_updated=plow.txTimeMillis() " +
            "WHERE " +
                "pk_proc=? " +
            "AND " +
                "pk_task=?";

    @Override
    public boolean updateProcRuntimeStats(RunningTask task) {
        float cores_used = task.cpuPercent / 100.0f;
        return jdbc.update(UPDATE_RUNTIME, task.rssMb, task.rssMb, task.progress, cores_used, cores_used, task.lastLog,
                UUID.fromString(task.procId), UUID.fromString(task.taskId)) == 1;
    }

    private static final String UPDATE_TASK =
            "UPDATE " +
                "plow.task " +
            "SET " +
                 "time_updated=plow.txTimeMillis(), " +
                 "int_last_ram_high=?,"+
                 "flt_last_cores_high=? " +
            "WHERE " +
                "pk_task=? " +
            "AND " +
                "int_state=?";

    @Override
    public boolean updateTaskRuntimeStats(RunningTask task) {
        float cores_used = task.cpuPercent / 100.0f;
        return jdbc.update(UPDATE_TASK, task.rssMb, cores_used,
                UUID.fromString(task.taskId), TaskState.RUNNING.ordinal()) == 1;
    }
}
