package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.TaskState;
import com.google.common.collect.Maps;
import com.google.common.primitives.Floats;

@Repository
public class DispatchDaoImpl extends AbstractDao implements DispatchDao {

    private static final String GET_DISPATCH_LAYERS =
            "SELECT " +
                "layer.pk_layer,"+
                "layer.pk_job,"+
                "layer.int_max_cores,"+
                "layer.int_min_cores,"+
                "layer.int_min_ram "+
            "FROM " +
                "plow.layer,"+
                "plow.layer_count " +
            "WHERE " +
                "layer.pk_layer = layer_count.pk_layer " +
            "AND " +
                "layer.pk_job = ? " +
            "AND " +
                "layer_count.int_waiting != 0 " +
            "AND " +
                "layer.int_min_cores <= ? " +
            "AND " +
                "layer.int_min_ram <= ? " +
            "AND " +
                "layer.str_tags && ? " +
            "ORDER BY " +
                "layer.int_order ASC,"+
                "layer_count.int_waiting DESC ";

    public static final RowMapper<DispatchLayer> DLAYER_MAPPER = new RowMapper<DispatchLayer>() {
        @Override
        public DispatchLayer mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchLayer layer = new DispatchLayer();
            layer.setLayerId(UUID.fromString(rs.getString("pk_layer")));
            layer.setJobId(UUID.fromString(rs.getString("pk_job")));
            layer.setMaxCores(rs.getInt("int_max_cores"));
            layer.setMinCores(rs.getInt("int_min_cores"));
            layer.setMinMemory(rs.getInt("int_min_ram"));
            return layer;
        }
    };

    @Override
    public List<DispatchLayer> getDispatchLayers(final Job job, final DispatchResource resource) {
        return jdbc.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(GET_DISPATCH_LAYERS);
                ps.setObject(1, job.getJobId());
                ps.setInt(2, resource.getCores());
                ps.setInt(3, resource.getMemory());
                ps.setArray(4, conn.createArrayOf("text", resource.getTags().toArray()));
                return ps;
            }
        }, DLAYER_MAPPER);
    }

    private static final String GET_DISPATCH_PROC =
            "SELECT " +
                "proc.pk_proc,"+
                "proc.pk_task,"+
                "proc.pk_node,"+
                "proc.pk_task,"+
                "proc.pk_quota,"+
                "proc.int_cores,"+
                "proc.int_ram, " +
                "node.str_tags,"+
                "node.str_name AS node_name, " +
                "task.str_name AS task_name, " +
                "task.pk_layer, " +
                "layer.pk_job " +
            "FROM " +
                "proc " +
            "INNER JOIN node ON proc.pk_node = node.pk_node " +
            "INNER JOIN task ON proc.pk_task = task.pk_task " +
            "INNER JOIN layer ON task.pk_layer = layer.pk_layer " +
            "WHERE " +
                "pk_proc = ?";

    public static final RowMapper<DispatchProc> DPROC_MAPPER = new RowMapper<DispatchProc>() {
        @Override
        public DispatchProc mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchProc proc = new DispatchProc();
            proc.setProcId((UUID) rs.getObject("pk_proc"));
            proc.setTaskId((UUID) rs.getObject("pk_task"));
            proc.setNodeId((UUID) rs.getObject("pk_node"));
            proc.setQuotaId((UUID) rs.getObject("pk_quota"));
            proc.setJobId((UUID) rs.getObject("pk_job"));
            proc.setLayerId((UUID) rs.getObject("pk_layer"));
            proc.setCores(rs.getInt("int_cores"));
            proc.setMemory(rs.getInt("int_ram"));
            proc.setTaskName(rs.getString("task_name"));
            proc.setHostname(rs.getString("node_name"));
            proc.setTags(new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray())));
            proc.setAllocated(true);
            return proc;
        }
    };

    @Override
    public DispatchProc getDispatchProc(UUID id) {
        return jdbc.queryForObject(GET_DISPATCH_PROC, DPROC_MAPPER, id);
    }

    public static final RowMapper<DispatchNode> DNODE_MAPPER = new RowMapper<DispatchNode>() {
        @Override
        public DispatchNode mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchNode node = new DispatchNode();
            node.setNodeId((UUID) rs.getObject("pk_node"));
            node.setClusterId((UUID) rs.getObject("pk_cluster"));
            node.setTags(new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray())));
            node.setCores(rs.getInt("int_idle_cores"));
            node.setMemory(rs.getInt("int_free_ram"));
            node.setName(rs.getString("str_name"));
            node.setDispatchable(true);
            return node;
        }
    };

    private static final String GET_DISPATCH_NODE =
            "SELECT " +
                "node.pk_node,"+
                "node.pk_cluster,"+
                "node.str_name,"+
                "node.str_tags,"+
                "node_dsp.int_idle_cores,"+
                "node_dsp.int_free_ram " +
            "FROM " +
                "plow.node," +
                "plow.node_dsp "+
            "WHERE " +
                "node.pk_node = node_dsp.pk_node " +
            "AND " +
                "node.str_name = ?";

    @Override
    public DispatchNode getDispatchNode(String name) {
        return jdbc.queryForObject(GET_DISPATCH_NODE, DNODE_MAPPER, name);
    }

    public static final RowMapper<DispatchProject> DPROJECT_MAPPER = new RowMapper<DispatchProject>() {
        @Override
        public DispatchProject mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchProject project = new DispatchProject();
            project.setProjectId((UUID) rs.getObject("pk_project"));
            project.setQuotaId((UUID) rs.getObject("pk_quota"));
            project.setTier(rs.getFloat("int_run_cores") / rs.getFloat("int_size"));
            return project;
        }
    };

    private static final String GET_SORTED_PROJECTS =
            "SELECT " +
                "quota.pk_project, " +
                "quota.pk_quota,"+
                "quota.int_run_cores,"+
                "quota.int_size " +
            "FROM " +
                "plow.quota,"+
                "plow.cluster " +
            "WHERE " +
                "quota.pk_cluster = cluster.pk_cluster " +
            "AND " +
                "quota.int_run_cores < quota.int_burst " +
            "AND " +
                "cluster.bool_locked IS FALSE " +
            "AND " +
                "quota.bool_locked IS FALSE " +
            "AND " +
                "cluster.pk_cluster = ?";

    @Override
    public List<DispatchProject> getSortedProjectList(final Node node) {
        List<DispatchProject> result =
                jdbc.query(GET_SORTED_PROJECTS, DPROJECT_MAPPER, node.getClusterId());

        Collections.sort(result, new Comparator<DispatchProject>() {
            @Override
            public int compare(DispatchProject o1, DispatchProject o2) {
                return Floats.compare(o1.getTier(), o2.getTier());
            }
        });

        return result;
    }

    public static final RowMapper<DispatchFolder> DFOLDER_MAPPER = new RowMapper<DispatchFolder>() {
        @Override
        public DispatchFolder mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchFolder folder = new DispatchFolder();
            folder.setFolderId((UUID)rs.getObject("pk_folder"));
            folder.setProjectId((UUID)rs.getObject("pk_project"));
            folder.setMinCores(rs.getInt("int_min_cores"));
            folder.setMaxCores(rs.getInt("int_max_cores"));
            folder.setRunCores(rs.getInt("int_run_cores"));
            return folder;
        }
    };

    private static final String GET_DFOLDER =
            "SELECT " +
                "folder.pk_folder,"+
                "folder.pk_project, " +
                "folder_dsp.int_min_cores,"+
                "folder_dsp.int_max_cores,"+
                "folder_dsp.int_run_cores "+
            "FROM " +
                "plow.folder,"+
                "plow.folder_dsp " +
            "WHERE " +
                "folder.pk_folder = folder_dsp.pk_folder " +
            "AND " +
                "folder.pk_folder = ?";

    @Override
    public DispatchFolder getDispatchFolder(UUID folder) {
        return jdbc.queryForObject(GET_DFOLDER, DFOLDER_MAPPER, folder);
    }

    public static final RowMapper<DispatchJob>DJOB_MAPPER =
            new RowMapper<DispatchJob>() {
        @Override
        public DispatchJob mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            DispatchJob job = new DispatchJob();
            job.setJobId((UUID) rs.getObject("pk_job"));
            job.setFolderId((UUID) rs.getObject("pk_folder"));
            job.setProjectId((UUID) rs.getObject("pk_project"));
            job.setName(rs.getString("str_name"));
            job.setWaitingFrames(rs.getInt("int_waiting"));
            job.setMaxCores(rs.getInt("int_max_cores"));
            job.setMinCores(rs.getInt("int_min_cores"));
            job.incrementCores(rs.getInt("int_run_cores"));

            return job;
        }
    };

    private static final String GET_DJOB =
            "SELECT " +
                "job.pk_job,"+
                "job.pk_folder, " +
                "job.pk_project, " +
                "job.str_name, " +
                "job_dsp.int_min_cores,"+
                "job_dsp.int_max_cores,"+
                "job_dsp.int_run_cores,"+
                "job_count.int_waiting " +
            "FROM " +
                "plow.job,"+
                "plow.job_dsp, " +
                "plow.job_count " +
            "WHERE " +
                "job.pk_job = job_dsp.pk_job " +
            "AND " +
                "job.pk_job = job_count.pk_job ";

    @Override
    public DispatchJob getDispatchJob(Job job) {
        return jdbc.queryForObject(GET_DJOB + " AND job.pk_job = ?"
                ,DJOB_MAPPER, job.getJobId());
    }

    @Override
    public List<DispatchJob> getDispatchJobs() {
        return jdbc.query(GET_DJOB + " AND job.int_state=?", DJOB_MAPPER,
                JobState.RUNNING.ordinal());
    }

    public static final RowMapper<DispatchTask>DTASK_MAPPER =
            new RowMapper<DispatchTask>() {
        @Override
        public DispatchTask mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            DispatchTask frame = new DispatchTask();
            frame.setTaskId((UUID)rs.getObject("pk_task"));
            frame.setLayerId((UUID) rs.getObject("pk_layer"));
            frame.setJobId((UUID) rs.getObject("pk_job"));
            frame.setName(rs.getString("str_name"));
            frame.setMinCores(rs.getInt("int_min_cores"));
            frame.setMinMemory(rs.getInt("int_min_ram"));
            frame.setTags(new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray())));
            frame.setName(rs.getString("str_name"));
            return frame;
        }
    };

    private static final String GET_DISPATCH_TASKS =
            "SELECT " +
                "task.pk_task,"+
                "task.str_name," +
                "layer.pk_layer, "+
                "layer.pk_job,"+
                "layer.int_min_cores,"+
                "layer.int_min_ram, " +
                "layer.str_name AS layer_name, " +
                "layer.str_tags " +
            "FROM " +
                "plow.layer " +
                    "INNER JOIN " +
                "plow.task " +
                    "ON layer.pk_layer = task.pk_layer " +
            "WHERE " +
                "layer.pk_layer = ? " +
            "AND " +
                "layer.int_min_cores <= ? " +
            "AND " +
                "layer.int_min_ram <= ? " +
            "AND " +
                "layer.str_tags && ? " +
            "AND " +
                "task.int_state = ? " +
            "AND " +
                "task.bool_reserved IS FALSE " +
            "ORDER BY " +
                "task.int_task_order, task.int_layer_order ASC " +
            "LIMIT ?";

    @Override
    public List<DispatchTask> getDispatchTasks(final DispatchLayer layer, final DispatchResource resource) {
        return jdbc.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(GET_DISPATCH_TASKS);
                ps.setObject(1, layer.getLayerId());
                ps.setInt(2, resource.getCores());
                ps.setInt(3, resource.getMemory());
                ps.setArray(4, conn.createArrayOf("text", resource.getTags().toArray()));
                ps.setInt(5, TaskState.WAITING.ordinal());
                ps.setInt(6, Defaults.DISPATCH_MAX_TASKS_PER_JOB);
                return ps;
            }
        }, DTASK_MAPPER);
    }

    private static final String GET_RUN_TASK =
            "SELECT " +
                "job.int_uid," +
                "job.str_username," +
                "job.str_log_path, " +
                "job.str_active_name AS job_name, " +
                "layer.str_command, " +
                "layer.str_name AS layer_name, " +
                "task.int_number, " +
                "task.str_name AS task_name, " +
                "task.int_retry " +
            "FROM " +
                "plow.task " +
                "INNER JOIN " +
                    "plow.layer " +
                        "ON layer.pk_layer = task.pk_layer " +
                "INNER JOIN " +
                    "plow.job " +
                        "ON layer.pk_job = job.pk_job " +
            "WHERE " +
                "task.pk_task = ? ";

    public static final RowMapper<RunTaskCommand> RUN_TASK_MAPPER =
            new RowMapper<RunTaskCommand>() {
        @Override
        public RunTaskCommand mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            RunTaskCommand task = new RunTaskCommand();
            task.logFile = String.format("%s/%s.%d.log",
                    rs.getString("str_log_path"), rs.getString("task_name"),
                    rs.getInt("int_retry"));
            task.uid = rs.getInt("int_uid");
            task.username = rs.getString("str_username");
            task.env = Maps.newHashMap();
            task.command = Arrays.asList((String[])rs.getArray("str_command").getArray());
            for (int i=0; i<task.command.size(); i++) {
                String part = task.command.get(i);
                part = part.replace("%{RANGE}", String.valueOf(rs.getInt("int_number")));
                part = part.replace("%{TASK}", rs.getString("task_name"));
                task.command.set(i, part);
            }

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
    public RunTaskCommand getRunTaskCommand(DispatchTask task, DispatchProc proc) {
        RunTaskCommand command = jdbc.queryForObject(
                GET_RUN_TASK, RUN_TASK_MAPPER, task.getTaskId());
        command.jobId = task.getJobId().toString();
        command.taskId = task.getTaskId().toString();
        command.procId = proc.getProcId().toString();
        command.layerId = task.getLayerId().toString();
        command.cores = proc.getCores();
        command.env.put("PLOW_TASK_ID", command.taskId);
        command.env.put("PLOW_JOB_ID", command.jobId);
        command.env.put("PLOW_PROC_ID", command.procId);
        command.env.put("PLOW_LAYER_ID", command.layerId);

        return command;
    }

    @Override
    public void addToDispatchTotals(DispatchProc proc) {
        jdbc.update(
                "UPDATE plow.folder_dsp SET int_run_cores=int_run_cores+? WHERE pk_folder=(" +
                        "SELECT pk_folder FROM job WHERE pk_job=?)", proc.getCores(), proc.getJobId());
        jdbc.update(
                "UPDATE plow.job_dsp SET int_run_cores=int_run_cores+? WHERE pk_job=?",
                proc.getCores(), proc.getJobId());
        jdbc.update("UPDATE layer_dsp SET int_run_cores=int_run_cores+? WHERE pk_layer=?",
                proc.getCores(), proc.getLayerId());
    }

    @Override
    public void subtractFromDispatchTotals(DispatchProc proc) {
        jdbc.update(
                "UPDATE plow.folder_dsp SET int_run_cores=int_run_cores-? WHERE pk_folder=(" +
                        "SELECT pk_folder FROM job WHERE pk_job=?)",  proc.getCores(), proc.getJobId());
        jdbc.update(
                "UPDATE plow.job_dsp SET int_run_cores=int_run_cores-? WHERE pk_job=?",
                proc.getCores(), proc.getJobId());
        jdbc.update("UPDATE layer_dsp SET int_run_cores=int_run_cores-? WHERE pk_layer=?",
                proc.getCores(), proc.getLayerId());
    }
}
