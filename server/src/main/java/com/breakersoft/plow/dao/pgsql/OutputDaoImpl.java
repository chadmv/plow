package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Output;
import com.breakersoft.plow.OutputE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.OutputDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class OutputDaoImpl extends AbstractDao implements OutputDao {

    private static final String GET_ATTRS =
            "SELECT " +
                "attrs " +
            "FROM " +
                "plow.output " +
            "WHERE " +
                "pk_output=?";

    @Override
    public Map<String, String> getAttrs(UUID outputId) {
        return jdbc.queryForObject(GET_ATTRS,new RowMapper<Map<String,String>>() {
            @Override
            public Map<String,String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                return (Map<String,String>) rs.getObject(1);
            }
        }, outputId);
    }

    @Override
    public Map<String, String> updateAttrs(UUID outputId,
            Map<String, String> attrs) {
        jdbc.update("UPDATE plow.output SET attrs = attrs || ? WHERE pk_output=?",
                attrs, outputId);
        return getAttrs(outputId);
    }

    @Override
    public Map<String, String> setAttrs(UUID outputId,
            Map<String, String> attrs) {
        jdbc.update("UPDATE plow.output SET attrs = ? WHERE pk_output=?",
                attrs, outputId);
        return getAttrs(outputId);
    }

    private static final String INSERT_OUTPUT =
            JdbcUtils.Insert("plow.output",
                    "pk_output", "pk_layer", "pk_job",
                    "str_path", "attrs");

    @Override
    public Output addOutput(final Layer layer, final String path, final Map<String,String> attrs) {

        final UUID id = UUID.randomUUID();

        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ret = conn.prepareStatement(INSERT_OUTPUT);
                ret.setObject(1, id);
                ret.setObject(2, layer.getLayerId());
                ret.setObject(3, layer.getJobId());
                ret.setString(4, path);
                ret.setObject(5, attrs);
                return ret;
            }
        });

        OutputE output = new OutputE();
        output.setId(id);
        output.setPath(path);
        output.setAttrs(attrs);
        return output;
    }
}
