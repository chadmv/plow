package com.breakersoft.plow.dao.pgsql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.DispatchTask;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.thrift.TaskState;

@Repository
public class DispatchDaoImpl extends AbstractDao implements DispatchDao {

    public static final RowMapper<DispatchJob>DJOB_MAPPER =
            new RowMapper<DispatchJob>() {
        @Override
        public DispatchJob mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            DispatchJob job = new DispatchJob();
            job.setJobId((UUID) rs.getObject("pk_job"));
            job.setFolderId((UUID) rs.getObject("pk_folder"));
            job.setProjectId((UUID) rs.getObject("pk_project"));
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
                "job_dsp.int_min_cores,"+
                "job_dsp.int_max_cores,"+
                "job_dsp.int_run_cores "+
            "FROM " +
                "job,"+
                "job_dsp " +
            "WHERE " +
                "job.pk_job = job_dsp.pk_job " +
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
            frame.setTaskId((UUID)rs.getObject("pk_frame"));
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
}
