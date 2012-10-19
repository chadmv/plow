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
            return cluster;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_cluster " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "pk_cluster = ?";

    @Override
    public Cluster getCluster(UUID id) {
        return jdbc.queryForObject(GET, MAPPER, id);
    }

    private static final String GET_BY_NAME =
            "SELECT " +
                "pk_cluster " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "str_name = ?";

    @Override
    public Cluster getCluster(String name) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, name);
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.cluster",
                    "pk_cluster",
                    "str_name",
                    "str_tag");

    @Override
    public Cluster create(final String name, final String tag) {
        final UUID id = UUID.randomUUID();
        jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ret = conn.prepareStatement(INSERT);
                ret.setObject(1, id);
                ret.setString(2, name);
                ret.setString(3, tag);
                return ret;
            }
        });

        ClusterE cluster = new ClusterE();
        cluster.setClusterId(id);
        return cluster;
    }

    private static final String GET_BY_DEFAULT =
            "SELECT " +
                "pk_cluster " +
            "FROM " +
                "plow.cluster " +
            "WHERE " +
                "bool_default = 't' " +
            "LIMIT 1";

    @Override
    public Cluster getDefaultCluster() {
        return jdbc.queryForObject(GET_BY_DEFAULT, MAPPER);
    }

    @Override
    public void setDefaultCluster(Cluster cluster) {
        jdbc.update("UPDATE cluster SET bool_default='f' WHERE bool_default='t'");
        jdbc.update("UPDATE cluster SET bool_default='t' WHERE pk_cluster=?", cluster.getClusterId());
    }

}
