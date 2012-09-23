package com.breakersoft.plow.dao.pgsql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.DispatchFolder;
import com.breakersoft.plow.dispatcher.DispatchLayer;
import com.breakersoft.plow.dispatcher.DispatchProject;
import com.breakersoft.plow.dispatcher.DispatchTask;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
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
                "layer.int_min_mem,"+
                "layer.str_tags,"+
                "layer_count.int_waiting " +
            "FROM " +
                "plow.layer,"+
                "plow.layer_count " +
            "WHERE " +
                "layer.pk_layer = layer_count.pk_layer " +
            "AND " +
                "layer.pk_job = ?";

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
            layer.setTags(new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray())));
            layer.setWaitingFrames(rs.getInt("int_waiting"));
            return layer;
        }
    };

    @Override
    public List<DispatchLayer> getDispatchLayers(Job job) {
        return jdbc.query(GET_DISPATCH_LAYERS, DLAYER_MAPPER, job.getJobId());
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
            node.setIdleCores(rs.getInt("int_free_cores"));
            node.setIdleMemory(rs.getInt("int_free_memory"));
            node.setName(rs.getString("str_name"));
            return node;
        }
    };

    private static final String GET_DISPATCH_HOST =
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
        return jdbc.queryForObject(GET_DISPATCH_HOST, DNODE_MAPPER, name);
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
    public boolean reserveFrame(Task frame) {
        return jdbc.update("UPDATE plow.frame SET bool_reserved=1 " +
                "WHERE pk_frame=? AND bool_reserved=0") == 1;
    }

    @Override
    public boolean unReserveFrame(Task frame) {
        return jdbc.update("UPDATE plow.frame SET bool_reserved=0 " +
                "WHERE pk_frame=? AND bool_reserved=1") == 1;
    }

    public static final RowMapper<DispatchTask>DFRAME_MAPPER =
            new RowMapper<DispatchTask>() {
        @Override
        public DispatchTask mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            DispatchTask frame = new DispatchTask();
            frame.setTaskId((UUID)rs.getObject("pk_task"));
            frame.setLayerId((UUID) rs.getObject("pk_layer"));
            frame.setNumber(rs.getInt("int_number"));
            frame.setCommand((String[]) rs.getArray("str_command").getArray());
            frame.setName(rs.getString("str_name"));
            return frame;
        }
    };

    private static final String GET_FRAMES =
            "SELECT " +
                "frame.pk_frame,"+
                "frame.str_name," +
                "frame.int_number," +
                "layer.pk_layer, "+
                "layer.str_command "+
            "FROM " +
                "plow.layer " +
                    "INNER JOIN " +
                "plow.frame " +
                    "ON layer.pk_layer = frame.pk_layer " +
            "WHERE " +
                "layer.pk_job = ? " +
            "AND " +
                "layer.min_cores <= ? " +
            "AND " +
                "layer.min_memory <= ? " +
            "AND " +
                "layer.str_tags && ? " +
            "AND " +
                "frame.int_state = ? " +
            "AND " +
                "frame.bool_reserved = 0 " +
            "ORDER BY " +
                "frame.int_frame_order ASC, "+
                "frame.int_layer_order ASC " +
            "LIMIT 20";

    @Override
    public List<DispatchTask> getFrames(final DispatchJob job, final DispatchNode node) {
        return jdbc.query(GET_FRAMES,
            new PreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps)
                        throws SQLException {
                    ps.setObject(1, job.getJobId());
                    ps.setInt(2, node.getIdleCores());
                    ps.setInt(3, node.getIdleMemory());
                    ps.setArray(4, ps.getConnection().createArrayOf(
                            "text", node.getTags().toArray()));
                    ps.setInt(5, TaskState.WAITING.ordinal());
                }
        }, DFRAME_MAPPER);
    }

    private static final String GET_FRAMES_BY_LAYER =
            "SELECT " +
                "task.pk_task,"+
                "task.str_name," +
                "task.int_number," +
                "layer.pk_layer, "+
                "layer.str_command "+
            "FROM " +
                "plow.layer " +
                    "INNER JOIN " +
                "plow.task " +
                    "ON layer.pk_layer = task.pk_layer " +
            "WHERE " +
                "layer.pk_layer = ? " +
            "AND " +
                "task.int_state = ? " +
            "AND " +
                "task.bool_reserved IS FALSE " +
            "ORDER BY " +
                "task.int_task_order ASC " +
            "LIMIT ?";

    @Override
    public List<DispatchTask> getFrames(final DispatchLayer layer, final DispatchNode node) {
        return jdbc.query(GET_FRAMES_BY_LAYER, DFRAME_MAPPER,
                layer.getLayerId(), TaskState.WAITING.ordinal(),
                Defaults.DISPATCH_MAX_TASKS_PER_JOB);
    }
}
