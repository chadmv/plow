package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.TaskE;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class TaskDoaImpl extends AbstractDao implements TaskDao {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(TaskDoaImpl.class);

    public static final RowMapper<Task> MAPPER = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            TaskE frame = new TaskE();
            frame.setTaskId(UUID.fromString(rs.getString(1)));
            frame.setLayerId(UUID.fromString(rs.getString(2)));
            frame.setJobId((UUID) rs.getObject(3));
            return frame;
        }
    };

    private static final String GET =
            "SELECT " +
                "task.pk_task,"+
                "task.pk_layer, " +
                "task.pk_job " +
            "FROM " +
                "plow.layer, " +
                "plow.task " +
            "WHERE " +
                "layer.pk_layer = task.pk_layer " ;

    @Override
    public Task get(UUID id) {
        return jdbc.queryForObject(
                GET + "AND task.pk_task=?",
                MAPPER, id);
    }

    @Override
    public Task get(Layer layer, int number) {
        return jdbc.queryForObject(
                GET + "AND layer.pk_layer=? AND task.int_number=?",
                MAPPER, layer.getLayerId(), number);
    }

    @Override
    public Task getByNameOrId(Job job, String identifer) {
        try {
            return get(UUID.fromString(identifer));
        } catch (IllegalArgumentException e) {
            return jdbc.queryForObject(
                    GET + "AND task.pk_job=? AND task.str_name=?",
                    MAPPER, job.getJobId(), identifer);
        }
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.task",
                    "pk_task", "pk_layer", "pk_job", "str_name",
                    "int_number", "int_task_order", "int_state");

    @Override
    public Task create(Layer layer, String name, int number, int taskOrder, int layerOrder) {
        final UUID id = UUID.randomUUID();

        jdbc.update(INSERT, id, layer.getLayerId(), layer.getJobId(), name,
                number, taskOrder, TaskState.INITIALIZE.ordinal());
        jdbc.update("INSERT INTO task_dsp (pk_task) VALUES (?)", id);

        TaskE task = new TaskE();
        task.setTaskId(id);
        task.setLayerId(layer.getLayerId());
        task.setJobId(layer.getJobId());
        return task;
    }

    @Override
    public boolean updateState(Task task, TaskState currentState, TaskState newState) {
        return jdbc.update("UPDATE plow.task SET int_state=?, " +
                "time_updated = txTimeMillis() WHERE pk_task=? AND int_state=?",
                newState.ordinal(), task.getTaskId(), currentState.ordinal()) == 1;
    }

    @Override
    public void clearLastLogLine(Task task) {
        jdbc.update("UPDATE plow.task_dsp SET str_last_log_line=? WHERE pk_task=?", "", task.getTaskId());
    }

    private static final String RESET_DSP =
            "UPDATE " +
                "plow.task_dsp " +
            "SET " +
                "int_try = int_try + 1,"+
                "int_cores=?,"+
                "int_ram=?,"+
                "int_used_ram=0,"+
                "int_progress=0,"+
                "str_last_node_name=?, "+
                "str_last_log_line=NULL " +
            "WHERE " +
                "pk_task=?";

    @Override
    public void resetTaskDispatchData(Task task, String host, int cores, int ram) {
        jdbc.update(RESET_DSP, cores, ram, host, task.getTaskId());
    }

    private static final String[] UPDATE_DSP = {
            "UPDATE " +
                "plow.task " +
            "SET " +
                "time_updated = txTimeMillis() " +
            "WHERE " +
                "pk_task=?::uuid " +
            "AND " +
                "int_state = ?",

            "UPDATE " +
                "plow.task_dsp "  +
            "SET " +
                "str_last_log_line=?," +
                "int_used_ram=?,"+
                "int_progress=? "+
            "WHERE " +
                "pk_task=?::uuid "
    };

    @Override
    public void updateTaskDispatchData(RunningTask runTask) {
        // TODO: fix this once rnd is pinging in max rss
        String lastLog = "";
        if (runTask.isSetLastLog()) {
            lastLog = runTask.lastLog;
        }

        if (jdbc.update(UPDATE_DSP[0], runTask.taskId,
                TaskState.RUNNING.ordinal()) > 0) {
            jdbc.update(UPDATE_DSP[1],
                lastLog, runTask.maxRss, (int)runTask.progress, runTask.taskId);
        }
    }

    @Override
    public boolean reserve(Task task) {
        return jdbc.update("UPDATE plow.task SET bool_reserved='t' " +
                "WHERE pk_task=? AND bool_reserved='f'", task.getTaskId()) == 1;
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
                "bool_reserved = 'f', " +
                "time_updated = txTimeMillis(), " +
                "time_started = txTimeMillis(), " +
                "time_stopped = 0 " +
            "WHERE " +
                "task.pk_task = ? " +
            "AND " +
                "int_state = ? " +
            "AND " +
                "bool_reserved = 't'";

    @Override
    public boolean start(Task task) {
        return jdbc.update(START_TASK,
                TaskState.RUNNING.ordinal(),
                task.getTaskId(),
                TaskState.WAITING.ordinal()) == 1;
    }

    public static final String STOP_TASK =
            "UPDATE " +
                "plow.task " +
            "SET " +
                "int_state = ?, " +
                "bool_reserved = 'f', " +
                "time_stopped = txTimeMillis(), " +
                "time_updated = txTimeMillis() " +
            "WHERE " +
                "task.pk_task = ? " +
            "AND " +
                "int_state = ? ";

    @Override
    public boolean stop(Task task, TaskState newState) {
        return jdbc.update(STOP_TASK,
                newState.ordinal(),
                task.getTaskId(),
                TaskState.RUNNING.ordinal()) == 1;
    }
}
