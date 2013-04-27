package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.ProcE;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class ProcDaoImpl extends AbstractDao implements ProcDao {

    public static final RowMapper<Proc> MAPPER = new RowMapper<Proc>() {
        @Override
        public Proc mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ProcE proc = new ProcE();
            proc.setProcId((UUID)rs.getObject(1));
            proc.setJobId((UUID)rs.getObject(2));
            proc.setNodeId((UUID)rs.getObject(3));
            proc.setTaskId((UUID)rs.getObject(4));
            proc.setHostname(rs.getString(5));
            return proc;
        }
    };

    private static final String GET =
            "SELECT " +
                "proc.pk_proc,"+
                "proc.pk_job,"+
                "proc.pk_node,"+
                "proc.pk_task,"+
                "node.str_name " +
            "FROM " +
                "plow.proc " +
                    "INNER JOIN plow.node " +
                "ON " +
                    "proc.pk_node = node.pk_node " ;

    private static final String GET_BY_ID = GET +"WHERE pk_proc = ?";
    @Override
    public Proc getProc(UUID procId) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, procId);
    }

    private static final String GET_BY_FR = GET +"WHERE pk_task = ?";
    @Override
    public Proc getProc(Task task) {
        return jdbc.queryForObject(GET_BY_FR, MAPPER, task.getTaskId());
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.proc",
                    "pk_proc",
                    "pk_node",
                    "pk_cluster",
                    "pk_quota",
                    "pk_task",
                    "pk_job",
                    "int_cores",
                    "int_ram");

    @Override
    public DispatchProc create(DispatchNode node, DispatchableTask task) {

        DispatchProc proc = new DispatchProc();
        proc.setProcId(UUID.randomUUID());
        proc.setJobId(task.jobId);
        proc.setTaskId(task.taskId);
        proc.setHostname(node.getName());
        proc.setNodeId(node.getNodeId());
        proc.setAllocated(true);
        proc.setTags(node.getTags());

        //TODO: make this smarter
        proc.setCores(task.minCores);
        proc.setMemory(task.minRam);

        // Requery for these in case they have changed.
        // In case we allow moving nodes while cores are running.
        UUID clusterId = jdbc.queryForObject("SELECT pk_cluster FROM plow.node WHERE pk_node=?", UUID.class, node.getNodeId());
        UUID quotaId = jdbc.queryForObject(
                "SELECT pk_quota FROM plow.quota WHERE quota.pk_project=? AND quota.pk_cluster = ?",
                UUID.class, task.getProjectId(), clusterId);

        proc.setClusterId(clusterId);
        proc.setQuotaId(quotaId);

        jdbc.update(INSERT,
                proc.getProcId(),
                node.getNodeId(),
                clusterId,
                quotaId,
                task.taskId,
                task.jobId,
                task.minCores,
                task.minRam);

        return proc;

    }

    private static final String UPDATE =
        "UPDATE " +
            "plow.proc " +
        "SET " +
            "pk_task = ?, " +
            "time_updated = plow.txTimeMillis() " +
        "WHERE " +
            "pk_proc = ?";


    @Override
    public void unassign(Proc proc) {
        jdbc.update(UPDATE, null, proc.getProcId());
    }

    public void assign(Proc proc, Task task) {
         jdbc.update(UPDATE,
                 task.getTaskId(), proc.getProcId());
    }

    @Override
    public boolean delete(Proc proc) {
        return jdbc.update(
                "DELETE FROM plow.proc WHERE pk_proc=?", proc.getProcId()) == 1;
    }

    @Override
    public List<Proc> getProcs(JobId job) {
        return jdbc.query(GET +
                " WHERE proc.pk_job=? ORDER BY proc.pk_proc", MAPPER, job.getJobId());
    }

    @Override
    public boolean setProcUnbooked(Proc proc, boolean unbooked) {
        return jdbc.update(
                "UPDATE plow.proc SET bool_unbooked=? WHERE proc.pk_proc=?",
                unbooked, proc.getProcId()) == 1;
    }
}
