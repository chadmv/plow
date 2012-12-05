package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.ProcE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.exceptions.ResourceAllocationException;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class ProcDaoImpl extends AbstractDao implements ProcDao {

    public static final RowMapper<Proc> MAPPER = new RowMapper<Proc>() {
        @Override
        public Proc mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ProcE proc = new ProcE();
            proc.setProcId((UUID)rs.getObject(1));
            proc.setQuotaId((UUID)rs.getObject(2));
            proc.setNodeId((UUID)rs.getObject(3));
            proc.setTaskId((UUID)rs.getObject(4));
            proc.setHostname(rs.getString(5));
            return proc;
        }
    };

    private static final String GET =
            "SELECT " +
                "proc.pk_proc,"+
                "proc.pk_quota,"+
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
    public Proc getProc(Task frame) {
        return jdbc.queryForObject(GET_BY_FR, MAPPER, frame.getTaskId());
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.proc",
                    "pk_proc",
                    "pk_quota",
                    "pk_node",
                    "pk_task",
                    "pk_job",
                    "int_cores",
                    "int_ram");

    @Override
    public void create(DispatchProc proc) {
        proc.setProcId(UUID.randomUUID());
        try {
            jdbc.update(INSERT,
                    proc.getProcId(),
                    proc.getQuotaId(),
                    proc.getNodeId(),
                    proc.getTaskId(),
                    proc.getJobId(),
                    proc.getCores(),
                    proc.getMemory());
        } catch (DataAccessException e) {
            throw new ResourceAllocationException(e);
        }
    }

    private static final String UPDATE =
        "UPDATE " +
            "plow.proc " +
        "SET " +
            "pk_task = ? " +
        "WHERE " +
            "pk_proc = ?";

    @Override
    public void update(DispatchProc proc, DispatchTask task) {
        if (task == null) {
            jdbc.update(UPDATE, null, proc.getProcId());
        }
        else {
            jdbc.update(UPDATE,
                task.getTaskId(), proc.getProcId());
        }
    }

    @Override
    public boolean delete(Proc proc) {
        return jdbc.update(
                "DELETE FROM plow.proc WHERE pk_proc=?", proc.getProcId()) == 1;
    }

    @Override
    public List<Proc> getProcs(Job job) {
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
