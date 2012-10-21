package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.LayerT;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;

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
            layer.minRamMb = rs.getInt("int_min_mem");
            layer.range = rs.getString("str_range");
            layer.tags = new HashSet<String>(
                    Arrays.asList((String[])rs.getArray("str_tags").getArray()));

            layer.totalTaskCount = rs.getInt("int_total");
            layer.succeededTaskCount = rs.getInt("int_succeeded");
            layer.runningTaskCount = rs.getInt("int_running");
            layer.deadTaskCount = rs.getInt("int_dead");
            layer.eatenTaskCount = rs.getInt("int_eaten");
            layer.waitingTaskCount = rs.getInt("int_waiting");
            layer.dependTaskCount = rs.getInt("int_depend");


            return layer;
        }
    };
    /*
    str_name VARCHAR(200) NOT NULL,
    str_range TEXT NOT NULL,
    str_command TEXT[] NOT NULL,
    str_tags TEXT[] NOT NULL,
    int_chunk_size INTEGER NOT NULL,
    int_order INTEGER NOT NULL,
    int_min_cores SMALLINT NOT NULL,
    int_max_cores SMALLINT NOT NULL,
    int_min_mem INTEGER NOT NULL
    */
    private static final String GET =
            "SELECT " +
                "layer.pk_layer,"+
                "layer.str_name,"+
                "layer.str_range,"+
                "layer.str_tags, " +
                "layer.int_chunk_size,"+
                "layer.int_min_cores,"+
                "layer.int_max_cores,"+
                "layer.int_min_mem, " +
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

    private static final String GET_BY_JOB =
            GET + " WHERE layer.pk_job = ?";

    @Override
    public List<LayerT> getLayers(UUID jobId) {
        return jdbc.query(GET_BY_JOB, MAPPER, jobId);
    }
}
