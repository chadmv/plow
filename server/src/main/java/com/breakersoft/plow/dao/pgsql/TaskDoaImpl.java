package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.TaskE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;
import com.breakersoft.plow.util.UUIDGen;
import com.google.common.collect.Lists;

@Repository
public class TaskDoaImpl extends AbstractDao implements TaskDao {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(TaskDoaImpl.class);

    public static final RowMapper<Task> MAPPER = new RowMapper<Task>() {
        @Override
        public Task mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            TaskE task = new TaskE();
            task.setTaskId(UUID.fromString(rs.getString(1)));
            task.setLayerId(UUID.fromString(rs.getString(2)));
            task.setJobId((UUID) rs.getObject(3));
            task.setName(rs.getString(4));
            return task;
        }
    };

    private static final String GET =
            "SELECT " +
                "task.pk_task,"+
                "task.pk_layer, " +
                "task.pk_job, " +
                "task.str_name " +
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
                    "pk_task",
                    "pk_layer",
                    "pk_job",
                    "str_name",
                    "int_number",
                    "int_task_order",
                    "int_layer_order",
                    "int_state",
                    "int_cores_min",
                    "int_ram_min");

    @Override
    public Task create(Layer layer, String name, int number, int taskOrder, int layerOrder, int minCores, int minRam) {
        final UUID id = UUIDGen.random();

        jdbc.update(INSERT, id, layer.getLayerId(), layer.getJobId(), name,
                number, taskOrder, layerOrder, TaskState.INITIALIZE.ordinal(),
                minCores, minRam);

        TaskE task = new TaskE();
        task.setTaskId(id);
        task.setLayerId(layer.getLayerId());
        task.setJobId(layer.getJobId());
        task.setName(name);
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
        jdbc.update("UPDATE plow.task_ping SET str_last_log_line=? WHERE pk_task=?", "", task.getTaskId());
    }

    @Override
    public boolean setTaskState(Task task, TaskState newState) {
        return jdbc.update("UPDATE plow.task SET int_state=?, bool_reserved='f', " +
                "time_updated = txTimeMillis() WHERE task.pk_task=?",
                newState.ordinal(), task.getTaskId()) == 1;
    }

    @Override
    public boolean setTaskState(Task task, TaskState newState, TaskState oldState) {
        return jdbc.update("UPDATE plow.task SET int_state=?, bool_reserved='f', " +
                "time_updated = txTimeMillis() WHERE task.pk_task=? AND int_state=?",
                newState.ordinal(), task.getTaskId(), oldState.ordinal()) == 1;
    }

    @Override
    public List<Task> getTasks(TaskFilterT filter) {

        final List<String> where = Lists.newArrayList();
        final List<Object> values = Lists.newArrayList();

        boolean idsIsSet = false;

        if (PlowUtils.isValid(filter.jobId)) {
            idsIsSet = true;
            where.add("task.pk_job = ?::uuid");
            values.add(filter.jobId);
        }

        if (filter.getLastUpdateTime() > 0) {
            where.add("task.time_updated >= ?");
            values.add(filter.getLastUpdateTime());
        }

        if (PlowUtils.isValid(filter.states)) {
            where.add(JdbcUtils.In("task.int_state", filter.states.size()));
            for (TaskState state: filter.states) {
                values.add(state.ordinal());
            }
        }

        if (PlowUtils.isValid(filter.layerIds)) {
            idsIsSet = true;
            where.add(JdbcUtils.In("task.pk_layer", filter.layerIds.size(), "uuid"));
            values.addAll(filter.layerIds);
        }

        if (PlowUtils.isValid(filter.taskIds)) {
            idsIsSet = true;
            where.add(JdbcUtils.In("task.pk_task", filter.taskIds.size(), "uuid"));
            values.addAll(filter.taskIds);
        }

        if (!idsIsSet) {
            throw new RuntimeException("A job ID, layer IDs or task IDs must be set.");
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        sb.append(" AND ");
        sb.append(StringUtils.join(where, " AND "));
        sb.append(" ORDER BY task.int_task_order ASC ");

        int limit = 1000;
        int offset = 0;

        if (filter.isSetLimit()) {
            if (filter.limit > 0 && filter.limit < Defaults.TASK_MAX_LIMIT) {
                limit = filter.limit;
            }
        }

        if (filter.isSetOffset()) {
            if (offset > 0) {
                offset = filter.offset;
            }
        }

        sb.append(JdbcUtils.limitOffset(limit, offset));

        final String q = sb.toString();
        logger.info(q);

        return jdbc.query(q, MAPPER, values.toArray());
    }
}
