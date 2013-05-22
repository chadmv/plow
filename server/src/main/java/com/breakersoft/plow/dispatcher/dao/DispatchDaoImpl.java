package com.breakersoft.plow.dispatcher.dao;

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
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.thrift.JobState;
import com.google.common.primitives.Floats;

@Repository
public class DispatchDaoImpl extends AbstractDao implements DispatchDao {

    private static final String BIG_DISPATCH_QUERY =
        "SELECT DISTINCT " +
            "job.pk_job, " +
            "job_dsp.float_tier, "+
            "folder_dsp.float_tier " +
        "FROM " +
            "plow.job," +
            "plow.job_dsp, " +
            "plow.folder_dsp, " +
            "plow.layer," +
            "plow.layer_count " +
        "WHERE " +
            "job.pk_job = job_dsp.pk_job " +
        "AND " +
            "job.pk_folder = folder_dsp.pk_folder " +
        "AND " +
            "job.pk_job = layer.pk_job " +
        "AND " +
            "layer.pk_layer = layer_count.pk_layer " +
        "AND " +
            "job.int_state  = ? " +
        "AND " +
            "bool_paused = 'f' " +
        "AND " +
            "job.pk_project = ? " +
        "AND " +
            "layer_count.int_waiting > 0 " +
        "AND " +
            "layer.str_tags && ? " +
        "AND " +
            "job_dsp.int_cores_max < job_dsp.int_cores_run " +
        "AND " +
            "folder_dsp.int_cores_max < folder_dsp.int_cores_run " +
        "ORDER BY " +
            "job_dsp.float_tier ASC, " +
            "folder_dsp.float_tier ASC";

    public static final RowMapper<DispatchJob> DISPATCH_JOB_MAPPER =
            new RowMapper<DispatchJob>() {
        @Override
        public DispatchJob mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchJob job = new DispatchJob();
            job.setJobId((UUID) rs.getObject(1));
            return job;
        }
    };

    @Override
    public List<DispatchJob> getDispatchJobs(final DispatchProject project, final DispatchNode node) {
        return jdbc.query(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ps = conn.prepareStatement(BIG_DISPATCH_QUERY);
                ps.setInt(1, JobState.RUNNING.ordinal());
                ps.setObject(2, project.getProjectId());
                ps.setArray(3, conn.createArrayOf("text", node.getTags().toArray()));
                return ps;
            }
        },DISPATCH_JOB_MAPPER);
    }

    @Override
    public DispatchJob getDispatchJob(UUID id) {
        return jdbc.queryForObject("SELECT pk_job FROM plow.job WHERE pk_job=?", DISPATCH_JOB_MAPPER, id);
    }

    private static final String GET_DISPATCH_PROC =
            "SELECT " +
                "proc.pk_proc,"+
                "proc.pk_task,"+
                "proc.pk_node,"+
                "proc.pk_quota,"+
                "proc.pk_cluster,"+
                "proc.int_cores,"+
                "proc.int_ram, " +
                "proc.bool_unbooked, " +
                "node.str_tags,"+
                "node.str_name AS node_name, " +
                "task.str_name AS task_name, " +
                "task.pk_job " +
            "FROM " +
                "proc " +
            "INNER JOIN node ON proc.pk_node = node.pk_node " +
            "INNER JOIN task ON proc.pk_task = task.pk_task ";

    private static final String GET_DISPATCH_PROC_BY_ID =
            GET_DISPATCH_PROC + " WHERE proc.pk_proc=?";

    public static final RowMapper<DispatchProc> DPROC_MAPPER = new RowMapper<DispatchProc>() {
        @Override
        public DispatchProc mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            DispatchProc proc = new DispatchProc();
            proc.setProcId((UUID) rs.getObject("pk_proc"));
            proc.setTaskId((UUID) rs.getObject("pk_task"));
            proc.setNodeId((UUID) rs.getObject("pk_node"));
            proc.setJobId((UUID) rs.getObject("pk_job"));
            proc.setClusterId((UUID) rs.getObject("pk_cluster"));
            proc.setQuotaId((UUID) rs.getObject("pk_quota"));
            proc.setCores(rs.getInt("int_cores"));
            proc.setMemory(rs.getInt("int_ram"));
            proc.setHostname(rs.getString("node_name"));
            proc.setTags(new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray())));
            proc.setAllocated(true);
            return proc;
        }
    };

    @Override
    public DispatchProc getDispatchProc(UUID id) {
        return jdbc.queryForObject(GET_DISPATCH_PROC_BY_ID, DPROC_MAPPER, id);
    }

    private static final String GET_DISPATCH_PROC_BY_TASK =
            GET_DISPATCH_PROC + " WHERE proc.pk_task=?";

    @Override
    public DispatchProc getDispatchProc(Task task) {
        return jdbc.queryForObject(GET_DISPATCH_PROC_BY_TASK, DPROC_MAPPER, task.getTaskId());
    }

    private static final String GET_ORPHAN_DISPATCH_PROCS =
            GET_DISPATCH_PROC +  "WHERE plow.currentTimeMillis() - proc.time_updated > ? LIMIT 100";

    @Override
    public List<DispatchProc> getOrphanProcs() {
        return jdbc.query(GET_ORPHAN_DISPATCH_PROCS, DPROC_MAPPER, Defaults.PROC_ORPHAN_CHECK_MILLIS);
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
            project.setTier(rs.getFloat("int_cores_run") / rs.getFloat("int_size"));
            project.setCode(rs.getString("str_code"));
            return project;
        }
    };

    private static final String GET_SORTED_PROJECTS =
            "SELECT " +
                "quota.pk_project, " +
                "quota.pk_quota,"+
                "quota.int_cores_run,"+
                "quota.int_size, " +
                "project.str_code " +
            "FROM " +
                "plow.quota,"+
                "plow.cluster, " +
                "plow.project " +
            "WHERE " +
                "quota.pk_cluster = cluster.pk_cluster " +
            "AND " +
                "quota.pk_project = project.pk_project " +
            "AND " +
                "quota.int_cores_run < quota.int_burst " +
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
}
