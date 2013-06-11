package com.breakersoft.plow.dispatcher.dao;

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
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
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
                    "pk_layer",
                    "pk_job",
                    "int_cores",
                    "int_ram");

    @Override
    public void create(DispatchProc proc) {
        proc.setProcId(UUID.randomUUID());

        jdbc.update(INSERT,
                proc.getProcId(),
                proc.getNodeId(),
                proc.getClusterId(),
                proc.getQuotaId(),
                proc.getTaskId(),
                proc.getLayerId(),
                proc.getJobId(),
                proc.getIdleCores(),
                proc.getIdleRam());
    }

    private static final String UNASSIGN =
        "UPDATE " +
            "plow.proc " +
        "SET " +
            "pk_task = NULL, " +
            "time_updated = plow.txTimeMillis() " +
        "WHERE " +
            "pk_proc = ? " +
        "AND " +
            "pk_task IS NOT NULL";

    @Override
    public boolean unassign(Proc proc) {
        return jdbc.update(UNASSIGN, proc.getProcId()) == 1;
    }

    private static final String ASSIGN =
            "UPDATE " +
                "plow.proc " +
            "SET " +
                "pk_task = ?, " +
                "pk_layer = ?, " +
                "time_updated = plow.txTimeMillis(), " +
                "time_started = plow.txTimeMillis()  " +
            "WHERE " +
                "pk_proc = ? " +
            "AND " +
                "pk_task IS NULL";

    public boolean assign(Proc proc, Task task) {
         return jdbc.update(ASSIGN,
                 task.getTaskId(), task.getLayerId(), proc.getProcId()) == 1;
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
