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
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.thrift.TaskT;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;
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
            task.name = rs.getString("str_name");
            task.number = rs.getInt("int_number");
            task.startTime = rs.getLong("time_started");
            task.stopTime = rs.getLong("time_stopped");
            task.state = TaskState.findByValue(rs.getInt("int_state"));
            task.cores = rs.getInt("int_cores");
            task.ramMb = rs.getInt("int_ram");
            task.maxRssMb = rs.getInt("int_max_rss");
            task.rssMb = rs.getInt("int_rss");
            task.maxRssMb = rs.getInt("int_max_rss");
            task.cpuPerc = rs.getShort("int_cpu_perc");
            task.maxCpuPerc = rs.getShort("int_max_cpu_perc");
            task.lastNodeName = rs.getString("str_last_node_name");
            task.lastLogLine = rs.getString("str_last_log_line");
            task.progress = rs.getInt("int_progress");
            task.retries = rs.getInt("int_retry");
            return task;
        }
    };

    private static final String GET =
        "SELECT " +
            "task.pk_task,"+
            "task.str_name,"+
            "task.int_number,"+
            "task.int_state,"+
            "task.int_depend_count, "+
            "task.int_task_order,"+
            "task.time_started, " +
            "task.time_stopped," +
            "task.time_updated,"+
            "task.int_retry,"+
            "task.int_cores,"+
            "task.int_ram,"+
            "task_ping.int_rss,"+
            "task_ping.int_max_rss,"+
            "task_ping.int_cpu_perc,"+
            "task_ping.int_max_cpu_perc,"+
            "task_ping.str_last_node_name,"+
            "task_ping.str_last_log_line,"+
            "task_ping.int_progress " +
        "FROM " +
            "task "+
        "INNER JOIN " +
            "task_ping ON task.pk_task = task_ping.pk_task ";

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
        List<String> where = Lists.newArrayList();
        List<Object> values = Lists.newArrayList();

        if (PlowUtils.isValid(filter.jobId)) {
            where.add("task.pk_job = ? ");
            values.add(UUID.fromString(filter.jobId));
        }
        else {
            throw new RuntimeException("At least a jobId must be set.");
        }

        if (filter.getLastUpdateTime() > 0) {
            where.add("task.time_updated >= ?");
            values.add(filter.getLastUpdateTime());
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        sb.append(" WHERE ");
        sb.append(StringUtils.join(where, " AND "));
        sb.append(" ORDER BY int_task_order ASC");

        return jdbc.query(sb.toString(), MAPPER, values.toArray());
    }
}
