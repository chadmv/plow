package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.LayerStatsT;
import com.breakersoft.plow.thrift.LayerT;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
@Transactional(readOnly=true)
public class ThriftLayerDaoImpl extends AbstractDao implements ThriftLayerDao {

    public static final RowMapper<LayerT> MAPPER = new RowMapper<LayerT>() {

        @Override
        public LayerT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final LayerStatsT stats = new LayerStatsT();
            stats.highRam = rs.getInt("int_ram_high");
            stats.avgRam = rs.getInt("int_ram_avg");
            stats.stdDevRam = rs.getDouble("flt_ram_std");
            stats.highCores = rs.getDouble("flt_cores_high");
            stats.avgCores = rs.getDouble("flt_cores_avg");
            stats.stdDevCores = rs.getDouble("flt_cores_std");

            stats.highCoreTime = rs.getInt("int_core_time_high");
            stats.lowCoreTime = rs.getInt("int_core_time_low");
            stats.avgCoreTime = rs.getInt("int_core_time_avg");
            stats.stdDevCoreTime = rs.getDouble("flt_core_time_std");
            stats.totalSuccessCoreTime = rs.getLong("int_total_core_time_success");
            stats.totalFailCoreTime = rs.getLong("int_total_core_time_fail");
            stats.totalCoreTime = stats.totalSuccessCoreTime + stats.totalFailCoreTime;

            stats.highClockTime = rs.getInt("int_clock_time_high");
            stats.lowClockTime = rs.getInt("int_clock_time_low");
            stats.avgClockTime = rs.getInt("int_clock_time_avg");
            stats.stdDevClockTime = rs.getDouble("flt_clock_time_std");
            stats.totalSuccessClockTime = rs.getLong("int_total_clock_time_success");
            stats.totalFailClockTime = rs.getLong("int_total_clock_time_fail");
            stats.totalClockTime = stats.totalSuccessClockTime + stats.totalFailClockTime;

            final LayerT layer = new LayerT();
            layer.setStats(stats);
            layer.setTotals(JdbcUtils.getTaskTotals(rs));

            layer.setId(rs.getString("pk_layer"));
            layer.setJobId(rs.getString("pk_job"));
            layer.name = rs.getString("str_name");
            layer.range = rs.getString("str_range");
            layer.chunk = rs.getInt("int_chunk_size");
            layer.tags = JdbcUtils.toList(rs.getArray("str_tags"));
            layer.threadable = rs.getBoolean("bool_threadable");
            layer.maxCores = rs.getInt("int_cores_max");
            layer.minCores = rs.getInt("int_cores_min");
            layer.minRam = rs.getInt("int_ram_min");
            layer.maxRam = rs.getInt("int_ram_max");
            layer.maxRetries = rs.getInt("int_retries_max");
            return layer;
        }
    };

    private static final String GET =
            "SELECT " +
                "layer.pk_layer,"+
                "layer.pk_job,"+
                "layer.str_name,"+
                "layer.str_range,"+
                "layer.str_tags, " +
                "layer.int_chunk_size,"+
                "layer.int_cores_min,"+
                "layer.int_cores_max,"+
                "layer.int_ram_min, " +
                "layer.int_ram_max, " +
                "layer.bool_threadable,"+
                "layer.int_retries_max,"+
                "layer_count.int_total, "+
                "layer_count.int_succeeded,"+
                "layer_count.int_running,"+
                "layer_count.int_dead,"+
                "layer_count.int_eaten,"+
                "layer_count.int_waiting,"+
                "layer_count.int_depend, "+
                "layer_dsp.int_cores_run,"+
                "layer_stat.int_ram_high,"+
                "layer_stat.int_ram_avg, " +
                "layer_stat.flt_ram_std, " +
                "layer_stat.flt_cores_high, " +
                "layer_stat.flt_cores_avg, " +
                "layer_stat.flt_cores_std, " +
                "layer_stat.int_core_time_high, " +
                "layer_stat.int_core_time_low, " +
                "layer_stat.int_core_time_avg, " +
                "layer_stat.flt_core_time_std, " +
                "layer_stat.int_total_core_time_success, " +
                "layer_stat.int_total_core_time_fail, " +
                "layer_stat.int_clock_time_high, " +
                "layer_stat.int_clock_time_low, " +
                "layer_stat.int_clock_time_avg, " +
                "layer_stat.flt_clock_time_std, " +
                "layer_stat.int_total_clock_time_success, " +
                "layer_stat.int_total_clock_time_fail " +
            "FROM " +
                "layer " +
            "INNER JOIN layer_count ON layer.pk_layer = layer_count.pk_layer " +
            "INNER JOIN layer_dsp ON layer.pk_layer = layer_dsp.pk_layer " +
            "INNER JOIN layer_stat ON layer.pk_layer = layer_stat.pk_layer ";

    private static final String GET_BY_ID =
            GET + " WHERE layer.pk_layer = ?";

    @Override
    public LayerT getLayer(UUID id) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, id);
    }

    private static final String GET_BY_NAME =
            GET + " WHERE layer.pk_job=? AND layer.str_name=?";

    @Override
    public LayerT getLayer(UUID jobId, String name) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, jobId, name);
    }

    private static final String GET_BY_JOB =
            GET + " WHERE layer.pk_job = ?";

    @Override
    public List<LayerT> getLayers(UUID jobId) {
        return jdbc.query(GET_BY_JOB, MAPPER, jobId);
    }
}
