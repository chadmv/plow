package com.breakersoft.plow.dispatcher.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.thrift.TaskState;
import com.google.common.collect.Maps;

@Repository
public class StatsDaoImpl extends AbstractDao implements StatsDao {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(StatsDaoImpl.class);


    private static final Comparator<RunningTask> SORT_BY_PROC = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.procId.compareTo(o2.procId);
        }
    };

    private static final Comparator<RunningTask> SORT_BY_TASK = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.taskId.compareTo(o2.taskId);
        }
    };

    private static final Comparator<RunningTask> SORT_BY_LAYER = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.layerId.compareTo(o2.layerId);
        }
    };

    private static final Comparator<RunningTask> SORT_BY_JOB = new Comparator<RunningTask>() {
        @Override
        public int compare(RunningTask o1, RunningTask o2) {
            return o1.jobId.compareTo(o2.jobId);
        }
    };

    private static final String UPDATE_PROC =
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

    private static final int[] PROC_BATCH_TYPES = new int[] {
        Types.INTEGER,
        Types.INTEGER,
        Types.INTEGER,
        Types.FLOAT,
        Types.FLOAT,
        Types.VARCHAR,
        Types.ARRAY,
        Types.OTHER,
        Types.OTHER
    };

    @Override
    public void batchUpdateProcRuntimeStats(List<RunningTask> tasks) {

        Collections.sort(tasks, SORT_BY_PROC);

        final Connection conn = DataSourceUtils.getConnection(jdbc.getDataSource());
        final BatchSqlUpdate batch = new BatchSqlUpdate(
                jdbc.getDataSource(), UPDATE_PROC, PROC_BATCH_TYPES);

        for (RunningTask task: tasks) {
            final float cores = task.cpuPercent / 100.0f;
            final Long[] io_stats = new Long[] { 0L, 0L, 0L, 0L };
            if (task.diskIO != null) {
                io_stats[0] = task.diskIO.readCount;
                io_stats[1] = task.diskIO.writeCount;
                io_stats[2] =  task.diskIO.readBytes;
                io_stats[3] =  task.diskIO.writeBytes;;
            }

            try {
                batch.update(
                        task.rssMb,
                        task.rssMb,
                        (int) Math.min(100, Math.round(task.progress)),
                        cores,
                        cores,
                        task.lastLog,
                        conn.createArrayOf("bigint", io_stats),
                        UUID.fromString(task.procId),
                        UUID.fromString(task.taskId));
            } catch (SQLException e) {
                // Will fail of createArrayOf fails
                logger.warn(
                        "Failed to update proc {} with task {} due to array formating, {}",
                        new Object[] { task.procId, task.taskId, e });
            }
        }

        batch.flush();
    }

    private static final int[] TASK_BATCH_TYPES = new int[] {
        Types.INTEGER,
        Types.FLOAT,
        Types.OTHER,
        Types.INTEGER
    };

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
    public void batchUpdateTaskRuntimeStats(List<RunningTask> tasks) {
        Collections.sort(tasks, SORT_BY_TASK);

        final int state = TaskState.RUNNING.ordinal();
        final BatchSqlUpdate batch = new BatchSqlUpdate(
                jdbc.getDataSource(), UPDATE_TASK, TASK_BATCH_TYPES);

        for (RunningTask task: tasks) {
            batch.update(
                    task.rssMb,
                    task.cpuPercent / 100.0f,
                    UUID.fromString(task.taskId),
                    state);
        }

        batch.flush();
    }

    private static final int[] LAYER_MEM_BATCH_TYPES = new int[] {
        Types.INTEGER,
        Types.OTHER,
        Types.INTEGER
    };
    private static final String UPDATE_LAYER_MEM =
            "UPDATE " +
                "plow.layer " +
            "SET " +
                "int_ram_min=? " +
            "WHERE " +
                "pk_layer = ? " +
            "AND " +
                "int_ram_min < ?";

    @Override
    public void batchUpdateLayerMinimumMemory(List<RunningTask> tasks) {
        Collections.sort(tasks, SORT_BY_LAYER);

        final BatchSqlUpdate batch = new BatchSqlUpdate(
                jdbc.getDataSource(), UPDATE_LAYER_MEM, LAYER_MEM_BATCH_TYPES);

        for (RunningTask task: tasks) {
            batch.update(task.rssMb,
                    UUID.fromString(task.layerId),
                    task.rssMb);
        }
        batch.flush();
    }

    private static final int[] BATCH_TYPES = new int[] {
        Types.INTEGER,
        Types.FLOAT,
        Types.OTHER
    };

    private static final String UPDATE_JOB_STAT =
            "UPDATE " +
                "plow.job_stat " +
            "SET " +
                "int_ram_high=?, " +
                "flt_cores_high=? " +
            "WHERE " +
                "pk_job = ? ";

    @Override
    public void batchUpdateJobRuntimeStats(List<RunningTask> tasks) {
        Collections.sort(tasks, SORT_BY_JOB);
        final Map<String, RuntimeStatsUpdate> map = Maps.newLinkedHashMap();

        for (RunningTask task: tasks) {
            RuntimeStatsUpdate update = map.get(task.jobId);

            if (update == null) {
                update = new RuntimeStatsUpdate(task.rssMb, task.cpuPercent);
                map.put(task.jobId, update);
                continue;
            }
            else {
                update.updateValues(task.rssMb, task.cpuPercent);
            }
        }

        final BatchSqlUpdate batch = new BatchSqlUpdate(
                jdbc.getDataSource(), UPDATE_JOB_STAT, BATCH_TYPES);

        for (Entry<String, RuntimeStatsUpdate> entry: map.entrySet()) {
            batch.update(
                    entry.getValue().memory,
                    entry.getValue().cpu / 100.0f,
                    UUID.fromString(entry.getKey()));
        }

        batch.flush();
    }

    private static final String UPDATE_LAYER_STAT =
            "UPDATE " +
                "plow.layer_stat " +
            "SET " +
                "int_ram_high=?, " +
                "flt_cores_high=? " +
            "WHERE " +
                "pk_layer = ? ";

    @Override
    public void batchUpdateLayerRuntimeStats(List<RunningTask> tasks) {

        Collections.sort(tasks, SORT_BY_LAYER);
        final Map<String, RuntimeStatsUpdate> map = Maps.newLinkedHashMap();

        for (RunningTask task: tasks) {
            RuntimeStatsUpdate update = map.get(task.layerId);

            if (update == null) {
                update = new RuntimeStatsUpdate(task.rssMb, task.cpuPercent);
                map.put(task.layerId, update);
                continue;
            }
            else {
                update.updateValues(task.rssMb, task.cpuPercent);
            }
        }

        final BatchSqlUpdate batch = new BatchSqlUpdate(
                jdbc.getDataSource(), UPDATE_LAYER_STAT, BATCH_TYPES);

        for (Entry<String, RuntimeStatsUpdate> entry: map.entrySet()) {
            batch.update(
                    entry.getValue().memory,
                    entry.getValue().cpu / 100.0f,
                    UUID.fromString(entry.getKey()));
        }

        batch.flush();
    }

    private final static class RuntimeStatsUpdate {

        private int memory;
        private short cpu;

        public RuntimeStatsUpdate(int memory, short cpu) {
            this.memory = memory;
            this.cpu = cpu;
        }

        public void updateValues(int memory, short cpu) {
            if (memory > this.memory) {
                this.memory = memory;
            }
            if (cpu > this.cpu) {
                this.cpu = cpu;
            }
        }
    }
}
