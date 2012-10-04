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
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
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
import com.breakersoft.plow.thrift.TaskState;
import com.google.common.primitives.Floats;

@Repository
public class DispatchDaoImpl extends AbstractDao implements DispatchDao {

    private static final String GET_DISPATCH_LAYERS =
            "SELECT " +
                "layer.pk_layer,"+
                "layer.pk_job,"+
                "layer.int_max_cores,"+
                "layer.int_min_cores,"+
                "layer.int_min_mem "+
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
                "layer.int_min_cores < ? " +
            "AND " +
                "layer.int_min_mem < ? " +
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
            layer.setMinMemory(rs.getInt("int_min_mem"));
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
                "proc.int_cores,"+
                "proc.int_mem, " +
                "node.str_name AS node_name, " +
                "task.str_name AS task_name " +
            "FROM " +
                "proc," +
                "node," +
                "task " +
            "WHERE " +
                "proc.pk_task = task.pk_task " +
            "AND " +
                "proc.pk_node = node.pk_node " +
            "AND " +
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
            proc.setCores(rs.getInt("int_cores"));
            proc.setMemory(rs.getInt("int_mem"));
            proc.setTaskName(rs.getString("task_name"));
            proc.setNodeName(rs.getString("node_name"));
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
            node.setCores(rs.getInt("int_free_cores"));
            node.setMemory(rs.getInt("int_free_memory"));
            node.setName(rs.getString("str_name"));
            return node;
        }
    };

    private static final String GET_DISPATCH_NODE =
            "SELECT " +
                "node.pk_node,"+
                "node.pk_cluster,"+
                "node.str_name,"+
                "node.str_tags,"+
                "node_dsp.int_free_cores,"+
                "node_dsp.int_free_memory " +
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
            folder.incrementCores(rs.getInt("int_run_cores"));
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
    public DispatchFolder getDispatchFolder(Folder folder) {
        return jdbc.queryForObject(GET_DFOLDER, DFOLDER_MAPPER, folder.getFolderId());
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
                "job.pk_job = job_count.pk_job " +
            "AND " +
                "job.pk_job = ?";

    @Override
    public DispatchJob getDispatchJob(Job job) {
        return jdbc.queryForObject(GET_DJOB, DJOB_MAPPER, job.getJobId());
    }

    @Override
    public boolean reserveTask(Task frame) {
        return jdbc.update("UPDATE plow.frame SET bool_reserved=1 " +
                "WHERE pk_frame=? AND bool_reserved=0") == 1;
    }

    @Override
    public boolean unreserveTask(Task frame) {
        return jdbc.update("UPDATE plow.frame SET bool_reserved=0 " +
                "WHERE pk_frame=? AND bool_reserved=1") == 1;
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
            frame.setNumber(rs.getInt("int_number"));
            frame.setCommand((String[]) rs.getArray("str_command").getArray());
            frame.setName(rs.getString("str_name"));
            frame.setMinCores(rs.getInt("int_min_cores"));
            frame.setMinMemory(rs.getInt("int_min_mem"));
            return frame;
        }
    };

    private static final String GET_TASKS =
            "SELECT " +
                "task.pk_task,"+
                "task.str_name," +
                "task.int_number," +
                "layer.pk_layer, "+
                "layer.pk_job,"+
                "layer.str_command, "+
                "layer.int_min_cores,"+
                "layer.int_min_mem " +
            "FROM " +
                "plow.layer " +
                    "INNER JOIN " +
                "plow.task " +
                    "ON layer.pk_layer = task.pk_layer " +
            "WHERE " +
                "layer.pk_layer = ? " +
            "AND " +
                "layer.int_min_cores < ? " +
            "AND " +
                "layer.int_min_mem < ? " +
            "AND " +
                "layer.str_tags && ? " +
            "AND " +
                "task.int_state = ? " +
            "AND " +
                "task.bool_reserved IS FALSE " +
            "ORDER BY " +
                "task.int_task_order ASC " +
            "LIMIT ?";

    @Override
    public List<DispatchTask> getDispatchTasks(final DispatchLayer layer, final DispatchResource resource) {
        return jdbc.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(GET_TASKS);
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
}
