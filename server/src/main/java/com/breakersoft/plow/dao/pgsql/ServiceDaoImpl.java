package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.ServiceE;
import com.breakersoft.plow.ServiceFull;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ServiceDao;
import com.breakersoft.plow.thrift.ServiceT;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;

@Repository
public class ServiceDaoImpl extends AbstractDao implements ServiceDao {

    public static final RowMapper<Service> MAPPER = new RowMapper<Service>() {
        @Override
        public Service mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ServiceE service = new ServiceE();
            service.setServiceId((UUID) rs.getObject(1));
            service.setName(rs.getString(2));
            return service;
        }
    };

    public static final RowMapper<ServiceFull> MAPPER_FULL = new RowMapper<ServiceFull>() {
        @Override
        public ServiceFull mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final ServiceFull service = new ServiceFull();
            service.setServiceId((UUID) rs.getObject("pk_service"));
            service.setName(rs.getString("str_name"));

            if (rs.getBoolean("isset_int_cores_min")) {
                service.setMinCores(rs.getInt("int_cores_min"));
            }

            if (rs.getBoolean("isset_int_cores_max")) {
                service.setMaxCores(rs.getInt("int_cores_max"));
            }

            if (rs.getBoolean("isset_int_ram_min")) {
                service.setMinCores(rs.getInt("int_ram_min"));
            }

            if (rs.getBoolean("isset_int_ram_max")) {
                service.setMinRam(rs.getInt("int_ram_max"));
            }

            if (rs.getBoolean("isset_int_ram_min")) {
                service.setMaxRam(rs.getInt("int_ram_min"));
            }

            if (rs.getBoolean("isset_bool_threadable")) {
                service.setThreadable(rs.getBoolean("bool_threadable"));
            }

            if (rs.getBoolean("isset_str_tags")) {
                service.setTags(JdbcUtils.toList(rs.getArray("str_tags")));
            }

            if (rs.getBoolean("isset_int_retries_max")) {
                service.setMaxRetries(rs.getInt("int_retries_max"));
            }

            return service;
        }
    };

    @Override
    public ServiceFull getServiceFull(String name) {
        try {
            return jdbc.queryForObject("SELECT * FROM plow.service WHERE str_name=?", MAPPER_FULL, name);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static final String GET =
        "SELECT " +
            "pk_service," +
            "str_name " +
        "FROM " +
            "service ";

    @Override
    public Service get(UUID id) {
        return jdbc.queryForObject(GET + "WHERE pk_service=?", MAPPER, id);
    }

    @Override
    public Service get(String name) {
        return jdbc.queryForObject(GET + "WHERE str_name=?", MAPPER, name);
    }

    @Override
    public boolean exists(String name) {
        return jdbc.queryForObject("SELECT COUNT(1) FROM plow.service WHERE str_name=?", Integer.class, name) > 0;
    }

    private static final String INSERT =
            JdbcUtils.Insert(
                    "plow.service",
                    "pk_service",
                    "str_name",
                    "str_tags",
                    "int_cores_min",
                    "int_cores_max",
                    "int_ram_min",
                    "int_ram_max",
                    "int_retries_max",
                    "bool_threadable",
                    "isset_int_cores_min",
                    "isset_int_cores_max",
                    "isset_int_ram_min",
                    "isset_int_ram_max",
                    "isset_str_tags",
                    "isset_bool_threadable",
                    "isset_int_retries_max");

    @Override
    public Service create(final ServiceT service) {

        final UUID id = UUID.randomUUID();

        jdbc.update(new PreparedStatementCreator() {
               @Override
               public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                   final PreparedStatement ret = conn.prepareStatement(INSERT);
                   ret.setObject(1, id);
                   ret.setString(2, service.getName());

                   if (PlowUtils.isValid(service.getTags())) {
                       ret.setArray(3, conn.createArrayOf("text",  PlowUtils.uniquify(service.getTags())));
                   }
                   else {
                       ret.setArray(3, null);
                   }
                   ret.setInt(4, service.getMinCores());
                   ret.setInt(5, service.getMaxCores());
                   ret.setInt(6, service.getMinRam());
                   ret.setInt(7, service.getMaxRam());
                   ret.setInt(8, service.getMaxRetries());
                   ret.setBoolean(9, service.isThreadable());

                   ret.setBoolean(10, service.isSetMinCores());
                   ret.setBoolean(11, service.isSetMaxCores());
                   ret.setBoolean(12, service.isSetMinRam());
                   ret.setBoolean(13, service.isSetMaxRam());
                   ret.setBoolean(14, service.isSetTags());
                   ret.setBoolean(15, service.isSetThreadable());
                   ret.setBoolean(16, service.isSetMaxRetries());

                   return ret;
               }
        });

        service.id = id.toString();

        ServiceE svc = new ServiceE();
        svc.setServiceId(id);
        svc.setName(service.name);
        return svc;
    }

    private static final String UPDATE =
            "UPDATE " +
                "service " +
            "SET " +
                "str_name = ?," +
                "str_tags = ?,"+
                "int_ram_min = ?,"+
                "int_ram_max = ?,"+
                "int_cores_min = ?,"+
                "int_cores_max = ?,"+
                "int_retries_max = ?, "+
                "bool_threadable = ?,"+
                "isset_int_cores_min=?,"+
                "isset_int_cores_max=?,"+
                "isset_int_ram_min=?,"+
                "isset_int_ram_max=?,"+
                "isset_str_tags=?,"+
                "isset_bool_threadable=?,"+
                "isset_int_retries_max=?" +
            "WHERE " +
                "pk_service = ?";

    @Override
    public boolean update(final ServiceT service) {
        return jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(final Connection conn) throws SQLException {
                final PreparedStatement ret = conn.prepareStatement(UPDATE);

                ret.setString(1, service.name);

                if (PlowUtils.isValid(service.getTags())) {
                    ret.setArray(2, conn.createArrayOf("text",
                            PlowUtils.uniquify(service.getTags())));
                }
                else {
                     ret.setArray(2, null);
                }

                ret.setInt(3, service.getMinRam());
                ret.setInt(4, service.getMaxRam());
                ret.setInt(5, service.getMinCores());
                ret.setInt(6, service.getMaxCores());
                ret.setInt(7, service.getMaxRetries());
                ret.setBoolean(8, service.isThreadable());
                ret.setBoolean(9, service.isSetMinCores());
                ret.setBoolean(10, service.isSetMaxCores());
                ret.setBoolean(11, service.isSetMinRam());
                ret.setBoolean(12, service.isSetMaxRam());
                ret.setBoolean(13, service.isSetTags());
                ret.setBoolean(14, service.isSetThreadable());
                ret.setBoolean(15, service.isSetMaxRetries());

                ret.setObject(16, UUID.fromString(service.id));
                return ret;
            }
        }) == 1;
    }

    @Override
    public boolean delete(UUID id) {
        return jdbc.update("DELETE FROM plow.service WHERE pk_service=?", id) == 1;
    }
}
