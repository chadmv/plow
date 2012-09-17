package com.breakersoft.plow.dao.pgsql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.DispatchTask;
import com.breakersoft.plow.dispatcher.DispatchJob;
import com.breakersoft.plow.dispatcher.DispatchNode;
import com.breakersoft.plow.thrift.TaskState;

@Repository
public class DispatchDaoImpl extends AbstractDao implements DispatchDao {

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
