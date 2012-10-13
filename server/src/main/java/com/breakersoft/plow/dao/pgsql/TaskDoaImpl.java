package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.TaskE;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.TaskDao;
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
                "layer.pk_job " +
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

    private static final String INSERT =
            JdbcUtils.Insert("plow.task",
                    "pk_task", "pk_layer", "str_name",
                    "int_number", "int_task_order",
                    "int_state");

    @Override
    public Task create(Layer layer, int number, int taskOrder, int layerOrder) {
        final UUID id = UUID.randomUUID();

        jdbc.update(INSERT, id, layer.getLayerId(), null,
                number, taskOrder, TaskState.INITIALIZE.ordinal());

        TaskE task = new TaskE();
        task.setTaskId(id);
        task.setLayerId(layer.getLayerId());
        task.setJobId(layer.getJobId());
        return task;
    }

    @Override
    public boolean updateState(Task task, TaskState currentState, TaskState newState) {
        return jdbc.update("UPDATE plow.task SET int_state=? WHERE pk_task=? AND int_state=?",
                newState.ordinal(), task.getTaskId(), currentState.ordinal()) == 1;
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
                "int_start_time = EXTRACT(epoch FROM NOW()), " +
                "int_stop_time = 0 " +
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
                "int_stop_time = EXTRACT(epoch FROM NOW()) " +
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
