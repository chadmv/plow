package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.ClusterE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class ClusterDaoImpl extends AbstractDao implements ClusterDao {

    @SuppressWarnings("unused")
    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(ClusterDaoImpl.class);

    public static final RowMapper<Cluster> MAPPER = new RowMapper<Cluster>() {
        @Override
        public Cluster mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ClusterE cluster = new ClusterE();
            cluster.setClusterId((UUID) rs.getObject(1));
            cluster.setName(rs.getString(2));
            return cluster;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_cluster, " +
                "str_name " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "pk_cluster = ?";

    @Override
    public Cluster get(UUID id) {
        return jdbc.queryForObject(GET, MAPPER, id);
    }

    private static final String GET_BY_NAME =
            "SELECT " +
                "pk_cluster, " +
                "str_name " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "str_name = ?";

    @Override
    public Cluster get(String name) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, name);
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.cluster",
                    "pk_cluster",
                    "str_name",
                    "str_tags");

    @Override
    public Cluster create(final String name, final String[] tags) {
        final UUID id = UUID.randomUUID();
        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ret = conn.prepareStatement(INSERT);
                ret.setObject(1, id);
                ret.setString(2, name);
                ret.setArray(3, conn.createArrayOf("text", tags));
                return ret;
            }
        });

        ClusterE cluster = new ClusterE();
        cluster.setClusterId(id);
        return cluster;
    }

    private static final String GET_BY_DEFAULT =
            "SELECT " +
                "pk_cluster, " +
                "str_name " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "bool_default = 't' " +
            "LIMIT 1";

    @Override
    public Cluster getDefault() {
        return jdbc.queryForObject(GET_BY_DEFAULT, MAPPER);
    }

    @Override
    public void setDefault(Cluster cluster) {
        jdbc.update("UPDATE cluster SET bool_default='f' WHERE bool_default='t'");
        jdbc.update("UPDATE cluster SET bool_default='t' WHERE pk_cluster=?", cluster.getClusterId());
    }

    @Override
    public boolean delete(Cluster c) {
        return jdbc.update("DELETE FROM plow.cluster WHERE pk_cluster=?", c.getClusterId()) == 1;
    }

    @Override
    public boolean setLocked(Cluster c, boolean value) {
        return jdbc.update("UPDATE plow.cluster SET bool_locked=? WHERE pk_cluster=? AND bool_locked=?",
                value, c.getClusterId(), !value) == 1;
    }

    @Override
    public void setName(Cluster c, String name) {
        jdbc.update("UPDATE plow.cluster SET str_name=? WHERE pk_cluster=?", name, c.getClusterId());
    }

    private static final String UPDATE_TAGS =
        "UPDATE " +
            "plow.cluster "+
        "SET " +
            "str_tags=? " +
        "WHERE " +
            "pk_cluster=?";

    @Override
    public void setTags(final Cluster c, final String[] tags) {
        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ret = conn.prepareStatement(UPDATE_TAGS);
                ret.setArray(1, conn.createArrayOf("text", tags));
                ret.setObject(2, c.getClusterId());
                return ret;
            }
        });
    }
}
