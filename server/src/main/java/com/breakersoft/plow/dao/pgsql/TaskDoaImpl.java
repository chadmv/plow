package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

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

    public static final RowMapper<Task> MAPPER = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            TaskE frame = new TaskE();
            frame.setTaskId(UUID.fromString(rs.getString(1)));
            frame.setLayerId(UUID.fromString(rs.getString(2)));
            return frame;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_task,"+
                "pk_layer " +
            "FROM " +
                "plow.task ";

    @Override
    public Task get(UUID id) {
        return jdbc.queryForObject(
                GET + "WHERE pk_task=?",
                MAPPER, id);
    }

    @Override
    public Task get(Layer layer, int number) {
        return jdbc.queryForObject(
                GET + "WHERE pk_layer=? AND int_number=?",
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
        return task;
    }
}
