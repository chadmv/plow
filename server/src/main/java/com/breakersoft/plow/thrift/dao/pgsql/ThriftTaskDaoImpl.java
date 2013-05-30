package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.thrift.TaskStatsT;
import com.breakersoft.plow.thrift.TaskT;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.collect.Lists;

@Repository
@Transactional(readOnly=true)
public class ThriftTaskDaoImpl extends AbstractDao implements ThriftTaskDao {

    public static final RowMapper<TaskT> MAPPER = new RowMapper<TaskT>() {

        @Override
        public TaskT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final TaskT task = new TaskT();
            task.id = rs.getString("pk_task");
            task.layerId = rs.getString("pk_layer");
            task.jobId = rs.getString("pk_job");
            task.state = TaskState.findByValue(rs.getInt("int_state"));
            task.name = rs.getString("str_name");
            task.number = rs.getInt("int_number");
            task.order = rs.getInt("int_task_order");
            task.retries = rs.getInt("int_retry");
            task.minCores = rs.getInt("int_cores_min");
            task.minRam = rs.getInt("int_ram_min");

            final TaskStatsT stats = new TaskStatsT();
            stats.startTime = rs.getLong("time_started");
            stats.stopTime = rs.getLong("time_stopped");
            stats.exitSignal = rs.getInt("int_exit_signal");
            stats.exitStatus = rs.getInt("int_exit_status");
            stats.lastNode = rs.getString("str_last_node_name");
            stats.ram = rs.getInt("int_last_ram");
            stats.highRam = rs.getInt("int_last_ram_high");
            stats.cores = rs.getInt("int_last_cores");
            stats.highCores = rs.getDouble("flt_last_cores_high");
            stats.retryNum = rs.getInt("int_retry");

            stats.active = false;
            task.setStats(stats);

            if (task.state.equals(TaskState.RUNNING)) {
                stats.cores = rs.getInt("int_cores");
                stats.highCores = rs.getDouble("flt_cores_high");
                stats.usedCores = rs.getDouble("flt_cores_used");
                stats.ram = rs.getInt("int_ram");
                stats.usedRam = rs.getInt("int_ram_used");
                stats.highRam = rs.getInt("int_ram_high");
                stats.progress = rs.getInt("int_progress");
                stats.lastLogLine = rs.getString("str_last_log_line");
                stats.active = true;
            }

            return task;
        }
    };

    // Make a func off task to gret the cores

    private static final String GET =
        "SELECT " +
            "task.pk_task,"+
            "task.pk_layer,"+
            "task.pk_job,"+
            "task.str_name,"+
            "task.int_number,"+
            "task.int_task_order,"+
            "task.int_state,"+
            "task.time_started, " +
            "task.time_stopped," +
            "task.int_retry,"+
            "task.int_last_ram,"+
            "task.int_last_ram_high,"+
            "task.int_last_cores,"+
            "task.flt_last_cores_high,"+
            "layer.int_cores_min,"+
            "task.int_ram_min,"+
            "task.str_last_node_name, " +
            "task.int_exit_signal,"+
            "task.int_exit_status,"+
            "proc.int_cores,"+
            "proc.flt_cores_used,"+
            "proc.flt_cores_high,"+
            "proc.int_ram,"+
            "proc.int_ram_high,"+
            "proc.int_ram_used,"+
            "proc.int_progress,"+
            "proc.str_last_log_line " +
        "FROM " +
            "task "+
        "INNER JOIN " +
            "layer ON task.pk_layer = layer.pk_layer " +
        "LEFT JOIN " +
            "plow.proc ON task.pk_task = proc.pk_proc ";

    private static final String GET_BY_ID =
        GET + " WHERE task.pk_task=?";

    @Override
    public TaskT getTask(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }

    private static final String GET_LOG_PATH =
        "SELECT " +
            "job.str_log_path || '/' || task.str_name || '.' || task.int_retry || '.log' " +
        "FROM " +
            "plow.task " +
            "INNER JOIN " +
                "plow.job " +
            "ON task.pk_job = job.pk_job " +
        "WHERE " +
            "task.pk_task = ?";

    @Override
    public String getLogPath(UUID id) {
        return jdbc.queryForObject(GET_LOG_PATH, String.class, id);
    }

    @Override
    public List<TaskT> getTasks(TaskFilterT filter) {
        final List<String> where = Lists.newArrayList();
        final List<Object> values = Lists.newArrayList();

        if (PlowUtils.isValid(filter.jobId)) {
            where.add("task.pk_job = ? ");
            values.add(UUID.fromString(filter.jobId));
        }

        if (PlowUtils.isValid(filter.layerIds)) {
            where.add(JdbcUtils.In(
                    "task.pk_layer", filter.layerIds.size()));
            values.addAll(filter.layerIds);
        }

        if (PlowUtils.isValid(filter.nodeIds)) {
            where.add(JdbcUtils.In(
                    "proc.pk_node", filter.nodeIds.size()));
            values.addAll(filter.nodeIds);
        }

        if (PlowUtils.isValid(filter.taskIds)) {
            where.add(JdbcUtils.In(
                    "task.pk_task", filter.taskIds.size()));
            values.addAll(filter.taskIds);
        }

        if (where.isEmpty()) {
             throw new RuntimeException("A job, layer, task, or node ID must be set.");
        }

        if (filter.getLastUpdateTime() > 0) {
            where.add("task.time_updated >= ?");
            values.add(filter.getLastUpdateTime());
        }

        if (PlowUtils.isValid(filter.states)) {
            where.add(JdbcUtils.In(
                    "task.int_state", filter.states.size()));
            for (TaskState state: filter.states) {
                values.add(state.ordinal());
            }
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        sb.append(" WHERE ");
        sb.append(StringUtils.join(where, " AND "));
        sb.append(" ORDER BY int_task_order ASC");

        return jdbc.query(sb.toString(), MAPPER, values.toArray());
    }

    public static final RowMapper<TaskStatsT> STATS_MAPPER = new RowMapper<TaskStatsT>() {

        @Override
        public TaskStatsT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final TaskStatsT stats = new TaskStatsT();
            stats.cores = rs.getInt("int_cores");
            stats.highCores = rs.getDouble("flt_cores_high");
            stats.usedCores = 0;
            stats.ram = rs.getInt("int_ram");
            stats.usedRam = 0;
            stats.highRam = rs.getInt("int_ram_high");
            stats.startTime = rs.getLong("time_started");
            stats.stopTime = rs.getLong("time_stopped");
            stats.retryNum = rs.getInt("int_retry");
            stats.progress = rs.getInt("int_progress");
            stats.lastLogLine = "";
            stats.exitSignal = rs.getInt("int_exit_signal");
            stats.exitStatus = rs.getInt("int_exit_status");
            stats.active = true;
            return stats;
        }
    };

    private static final String TASK_HISTORY =
        "SELECT " +
            "int_cores,"+
            "int_cores,"+
            "flt_cores_high,"+
            "int_ram,"+
            "int_ram_high,"+
            "time_started,"+
            "time_stopped,"+
            "int_retry,"+
            "int_progress,"+
            "int_exit_signal,"+
            "int_exit_status "+
       "FROM " +
            "task_history " +
       "WHERE " +
            "pk_task = ?";

    @Override
    public List<TaskStatsT> getTaskStats(UUID taskId) {
        return jdbc.query(TASK_HISTORY, STATS_MAPPER, taskId);
    }
}
