package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.OutputT;
import com.breakersoft.plow.thrift.dao.ThriftOutputDao;

@Repository
@Transactional(readOnly=true)
public class ThriftOutputDaoImpl extends AbstractDao implements ThriftOutputDao {

    private static final ParameterizedRowMapper<OutputT> MAPPER =
            new ParameterizedRowMapper<OutputT>() {
        @Override
        public OutputT mapRow(ResultSet rs, int rowNum) throws SQLException {
            final OutputT output = new OutputT();
            output.setOutputId(rs.getString("pk_output"));
            output.setPath(rs.getString("str_path"));
            output.setAttrs((Map<String, String>) rs.getObject("attrs"));
            return output;
        }
    };

    private static final String GET =
            "SELECT " +
                "output.pk_output,"+
                "output.pk_job,"+
                "output.pk_layer," +
                "output.str_path,"+
                "output.attrs " +
            "FROM " +
                "plow.output ";

    @Override
    public OutputT getOutput(UUID id) {
        return jdbc.queryForObject(GET + " WHERE pk_object=?", MAPPER, id);
    }

    @Override
    public List<OutputT> getLayerOutputs(UUID layerId) {
        return jdbc.query(GET + " WHERE output.pk_layer=?", MAPPER, layerId);
    }

    @Override
    public List<OutputT> getJobOutputs(UUID jobId) {
        return jdbc.query(GET + " WHERE output.pk_job=?", MAPPER, jobId);
    }

    @Override
    public List<OutputT> getOutputs(Layer layer) {
        return getLayerOutputs(layer.getLayerId());
    }

    @Override
    public List<OutputT> getOutputs(Job job) {
        return getJobOutputs(job.getJobId());
    }
}
