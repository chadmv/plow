package com.breakersoft.plow.dispatcher.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.thrift.TaskState;
import com.google.common.collect.Maps;

@Repository
public class DispatchTaskDaoImpl extends AbstractDao implements DispatchTaskDao {

    private static final String RETRY =
            "SELECT " +
                "layer.int_retries_max - task.int_retry " +
            "FROM " +
                "task,"+
                "layer " +
            "WHERE " +
                "task.pk_layer = layer.pk_layer " +
            "AND " +
                "task.pk_task = ?";

    @Override
    public boolean isAtMaxRetries(Task task) {
        return jdbc.queryForObject(RETRY, Integer.class, task.getTaskId()) <= 0;
    }

    @Override
    public boolean reserve(Task task) {
        try {
            jdbc.queryForObject("SELECT task.pk_task FROM plow.task WHERE pk_task=? AND int_state=? AND bool_reserved='f' FOR UPDATE NOWAIT",
                    String.class, task.getTaskId(), TaskState.WAITING.ordinal());
            return jdbc.update("UPDATE plow.task SET bool_reserved='t' " +
                    "WHERE pk_task=? AND int_state=? AND bool_reserved='f'", task.getTaskId(), TaskState.WAITING.ordinal()) == 1;
        } catch (Exception e) {
            System.out.println("Could not reserve: "  + e.toString());
            return false;
        }
    }

    @Override
    public boolean unreserve(Task task) {
        return jdbc.update("UPDATE plow.task SET bool_reserved='f' " +
                "WHERE pk_task=? AND bool_reserved='t'", task.getTaskId()) == 1;
    }

    public static final String START_TASK =
            "UPDATE " +
                "plow.task " +
            "SET " +
                "int_state = ?, " +
                "bool_reserved = 'f',"+
                "int_retry=int_retry+1,"+
                "time_updated = txTimeMillis(), " +
                "time_started = txTimeMillis(), " +
                "time_stopped = 0, " +
                "str_last_node_name=?,"+
                "int_last_ram=?,"+
                "int_last_ram_high=0,"+
                "int_last_cores=?,"+
                "flt_last_cores_high=0" +
            "WHERE " +
                "task.pk_task = ? " +
            "AND " +
                "int_state = ? " +
            "AND " +
                "bool_reserved = 't'";

    @Override
    public boolean start(Task task, DispatchProc proc) {
        return jdbc.update(START_TASK,
                TaskState.RUNNING.ordinal(),
                proc.getHostname(),
                proc.getIdleRam(),
                proc.getIdleCores(),
                task.getTaskId(),
                TaskState.WAITING.ordinal()) == 1;
    }

    public static final String STOP_TASK =
            "UPDATE " +
                "plow.task " +
            "SET " +
                "int_state = ?, " +
                "bool_reserved = 'f', " +
                "time_stopped = currentTimeMillis(), " +
                "time_updated = currentTimeMillis(), " +
                "int_exit_status=?," +
                "int_exit_signal=? " +
            "WHERE " +
                "task.pk_task = ? " +
            "AND " +
                "int_state = ? ";

    @Override
    public boolean stop(Task task, TaskState newState, int exitStatus, int exitSignal) {
        return jdbc.update(STOP_TASK,
                newState.ordinal(),
                exitStatus,
                exitSignal,
                task.getTaskId(),
                TaskState.RUNNING.ordinal()) == 1;
    }

    private static final String GET_DISPATCHABLE_TASKS =
            "SELECT " +
                "task.pk_task,"+
                "task.pk_layer,"+
                "task.pk_job,"+
                "task.int_ram_min,  " +
                "task.str_name," +
                "layer.int_cores_min,"+
                "job.pk_project " +
            "FROM " +
                "plow.layer " +
                    "INNER JOIN " +
                "plow.task ON layer.pk_layer = task.pk_layer " +
                    "INNER JOIN " +
                "plow.job ON layer.pk_job = job.pk_job " +
            "WHERE " +
                "layer.pk_job = ? " +
            "AND " +
                "layer.int_cores_min <= ? " +
            "AND " +
                "layer.str_tags && ? " +
            "AND " +
                "task.int_state = ? " +
            "AND " +
                "task.int_ram_min <= ? " +
            "AND " +
                "task.bool_reserved IS FALSE " +
            "ORDER BY " +
                "task.int_task_order, task.int_layer_order ASC " +
            "LIMIT 10";

    public static final RowMapper<DispatchTask> DISPATCHABLE_TASK_MAPPER =
            new RowMapper<DispatchTask>() {
        @Override
        public DispatchTask mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchTask task = new DispatchTask();
            task.projectId = (UUID) rs.getObject("pk_project");
            task.taskId = (UUID) rs.getObject("pk_task");
            task.layerId = (UUID) rs.getObject("pk_layer");
            task.jobId = (UUID) rs.getObject("pk_job");
            task.minCores = rs.getInt("int_cores_min");
            task.minRam = rs.getInt("int_ram_min");
            task.name = rs.getString("str_name");
            return task;
        }
    };

    @Override
    public List<DispatchTask> getDispatchableTasks(final JobId job, final DispatchResource resource) {
        return jdbc.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(GET_DISPATCHABLE_TASKS);
                ps.setObject(1, job.getJobId());
                ps.setInt(2, resource.getIdleCores());
                ps.setArray(3, conn.createArrayOf("text", resource.getTags().toArray()));
                ps.setInt(4, TaskState.WAITING.ordinal());
                ps.setInt(5, resource.getIdleRam());
                return ps;
            }
        }, DISPATCHABLE_TASK_MAPPER);
    }

    private static final String GET_RUN_TASK =
        "SELECT " +
            "job.int_uid," +
            "job.str_username," +
            "job.str_log_path, " +
            "job.str_active_name AS job_name, " +
            "job.hstore_env AS job_env, " +
            "layer.str_command, " +
            "layer.str_name AS layer_name, " +
            "layer.hstore_env AS layer_env, " +
            "task.int_number, " +
            "task.pk_task,"+
            "task.pk_layer,"+
            "task.pk_job,"+
            "task.str_name AS task_name, " +
            "task.int_retry, " +
            "proc.pk_proc,"+
            "proc.int_cores " +
        "FROM " +
            "plow.task " +
                "INNER JOIN plow.proc ON task.pk_task = proc.pk_task " +
                "INNER JOIN plow.layer ON layer.pk_layer = task.pk_layer " +
                "INNER JOIN plow.job ON layer.pk_job = job.pk_job " +
        "WHERE " +
            "task.pk_task = ? ";

    public static final RowMapper<RunTaskCommand> RUN_TASK_MAPPER =
            new RowMapper<RunTaskCommand>() {

        @Override
        public RunTaskCommand mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            RunTaskCommand task = new RunTaskCommand();
            task.jobId = rs.getString("pk_job");
            task.taskId = rs.getString("pk_task");
            task.layerId = rs.getString("pk_layer");
            task.procId = rs.getString("pk_proc");
            task.cores = rs.getInt("int_cores");

            task.logFile = String.format("%s/%s.%d.log",
                    rs.getString("str_log_path"), rs.getString("task_name"),
                    rs.getInt("int_retry"));
            task.uid = rs.getInt("int_uid");
            task.username = rs.getString("str_username");
            task.command = Arrays.asList((String[])rs.getArray("str_command").getArray());

            for (int i=0; i<task.command.size(); i++) {
                String part = task.command.get(i);
                part = part.replace("%{RANGE}", String.valueOf(rs.getInt("int_number")));
                part = part.replace("%{TASK}", rs.getString("task_name"));
                task.command.set(i, part);
            }

            task.env = Maps.newHashMap();
            Map<String,String> job_env = (Map<String, String>) rs.getObject("job_env");
            if (job_env != null) {
                task.env.putAll(job_env);
            }

            Map<String,String> layer_env = (Map<String, String>) rs.getObject("layer_env");
            if (layer_env != null) {
                task.env.putAll(layer_env);
            }

            task.env.put("PLOW_TASK_ID", rs.getString("pk_task"));
            task.env.put("PLOW_JOB_ID", rs.getString("pk_job"));
            task.env.put("PLOW_PROC_ID", rs.getString("pk_proc"));
            task.env.put("PLOW_LAYER_ID", rs.getString("pk_layer"));
            task.env.put("PLOW_JOB_NAME", rs.getString("job_name"));
            task.env.put("PLOW_LAYER_NAME", rs.getString("layer_name"));
            task.env.put("PLOW_TASK_NAME", rs.getString("task_name"));
            task.env.put("PLOW_LOG_DIR", rs.getString("str_log_path"));
            task.env.put("PLOW_UID", rs.getString("int_uid"));
            task.env.put("PLOW_TASK_NUMBER", rs.getString("int_number"));

            return task;
        }
    };

    @Override
    public RunTaskCommand getRunTaskCommand(Task task) {
        return jdbc.queryForObject(
                GET_RUN_TASK, RUN_TASK_MAPPER, task.getTaskId());
    }
}
