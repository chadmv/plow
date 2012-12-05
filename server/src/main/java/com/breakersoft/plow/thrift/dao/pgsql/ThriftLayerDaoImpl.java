package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.LayerT;
import com.breakersoft.plow.thrift.OutputT;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
@Transactional(readOnly = true)
public class ThriftLayerDaoImpl extends AbstractDao implements ThriftLayerDao {

    public static final RowMapper<LayerT> MAPPER = new RowMapper<LayerT>() {

        @Override
        public LayerT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            LayerT layer = new LayerT();
            layer.id = rs.getString("pk_layer");
            layer.name = rs.getString("str_name");
            layer.chunk = rs.getInt("int_chunk_size");
            layer.maxCores = rs.getInt("int_max_cores");
            layer.minCores = rs.getInt("int_min_cores");
            layer.minRamMb = rs.getInt("int_min_ram");
            layer.range = rs.getString("str_range");
            layer.tags = new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray()));

            layer.setTotals(JdbcUtils.getTaskTotals(rs));
            return layer;
        }
    };

    private static final String GET =
            "SELECT " +
                "layer.pk_layer,"+
                "layer.str_name,"+
                "layer.str_range,"+
                "layer.str_tags, " +
                "layer.int_chunk_size,"+
                "layer.int_min_cores,"+
                "layer.int_max_cores,"+
                "layer.int_min_ram, " +
                "layer_count.int_total, "+
                "layer_count.int_succeeded,"+
                "layer_count.int_running,"+
                "layer_count.int_dead,"+
                "layer_count.int_eaten,"+
                "layer_count.int_waiting,"+
                "layer_count.int_depend "+
            "FROM " +
                "layer " +
            "INNER JOIN layer_count ON layer.pk_layer = layer_count.pk_layer ";

    private static final String GET_BY_ID =
            GET + " WHERE layer.pk_layer = ?";

    @Override
    public LayerT getLayer(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }

    private static final String GET_BY_NAME =
            GET + " WHERE layer.str_name=? AND layer.pk_job=?";

    @Override
    public LayerT getLayer(UUID jobId, String name) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, name, jobId);
    }

    private static final String GET_BY_JOB =
            GET + " WHERE layer.pk_job = ?";

    @Override
    public List<LayerT> getLayers(UUID jobId) {
        return jdbc.query(GET_BY_JOB, MAPPER, jobId);
    }

    public static final RowMapper<OutputT> OUT_MAPPER = new RowMapper<OutputT>() {
        @Override
        public OutputT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            OutputT output = new OutputT();
            output.setPath(rs.getString("str_path"));
            output.setAttrs((Map<String, String>) rs.getObject("attrs"));
            return output;
        }
    };

    @Override
    public List<OutputT> getOutputs(UUID layerId) {
        return jdbc.query("SELECT str_path, attrs FROM output WHERE pk_layer=?",
                OUT_MAPPER, layerId);
    }
}
