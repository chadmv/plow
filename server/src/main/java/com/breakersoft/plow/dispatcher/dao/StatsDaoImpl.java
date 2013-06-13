package com.breakersoft.plow.dispatcher.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
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
                "int_io_stats=?,"+
                "time_updated=plow.txTimeMillis() " +
            "WHERE " +
                "pk_proc=? " +
            "AND " +
                "pk_task=?";

    @Override
    public boolean updateProcRuntimeStats(final RunningTask task) {

        return jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final float cores_used = task.cpuPercent / 100.0f;
                final Long[] io_stats = {
                    task.diskIO.readCount,
                    task.diskIO.writeCount,
                    task.diskIO.readBytes,
                    task.diskIO.writeBytes
                };

                final PreparedStatement ret = conn.prepareStatement(UPDATE_RUNTIME);
                ret.setInt(1,  task.rssMb);
                ret.setInt(2,  task.rssMb);
                ret.setInt(3,  (int) Math.min(100, Math.round(task.progress)));
                ret.setFloat(4, cores_used);
                ret.setFloat(5, cores_used);
                ret.setString(6, task.lastLog);
                ret.setArray(7,  conn.createArrayOf("bigint", io_stats));
                ret.setObject(8, UUID.fromString(task.procId));
                ret.setObject(9, UUID.fromString(task.taskId));
                return ret;
            }
        }) == 1;
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
