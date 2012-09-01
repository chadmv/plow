package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Frame;
import com.breakersoft.plow.FrameE;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.FrameDao;
import com.breakersoft.plow.thrift.FrameState;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class FrameDoaImpl extends AbstractDao implements FrameDao {

    public static final RowMapper<Frame> MAPPER = new RowMapper<Frame>() {
        @Override
        public Frame mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            FrameE frame = new FrameE();
            frame.setFrameId(UUID.fromString(rs.getString(1)));
            frame.setLayerId(UUID.fromString(rs.getString(2)));
            return frame;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_frame,"+
                "pk_layer " +
            "FROM " +
                "plow.frame ";

    @Override
    public Frame get(UUID id) {
        return jdbc.queryForObject(
                GET + "WHERE pk_frame=?",
                MAPPER, id);
    }

    @Override
    public Frame get(Layer layer, int number) {
        return jdbc.queryForObject(
                GET + "WHERE pk_layer=? AND int_number=?",
                MAPPER, layer.getLayerId(), number);
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.frame",
                    "pk_frame", "pk_layer", "str_alias",
                    "int_number", "int_order", "int_state");

    @Override
    public Frame create(Layer layer, int number, int order) {
        final UUID id = UUID.randomUUID();

        jdbc.update(INSERT, id, layer.getLayerId(), null,
                number, order, FrameState.INITIALIZE.ordinal());

        FrameE frame = new FrameE();
        frame.setFrameId(id);
        frame.setLayerId(layer.getLayerId());
        return frame;
    }
}
