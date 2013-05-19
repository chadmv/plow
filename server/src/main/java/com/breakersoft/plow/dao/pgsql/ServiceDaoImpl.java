package com.breakersoft.plow.dao.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.ServiceE;
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

    private static final String GET =
        "SELECT " +
            "pk_service," +
            "str_name " +
        "FROM " +
            "service ";

    public Service get(UUID id) {
        return jdbc.queryForObject(GET + "WHERE pk_service=?", MAPPER, id);
    }

    public Service get(String name) {
        return jdbc.queryForObject(GET + "WHERE str_name=?", MAPPER, name);
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
                    "bool_threadable");

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
                       ret.setArray(3, conn.createArrayOf("text", new String[] { }));
                   }
                   ret.setInt(4, service.getMinCores());
                   ret.setInt(5, service.getMaxCores());
                   ret.setInt(6, service.getMinRam());
                   ret.setInt(7, service.getMaxRam());
                   ret.setInt(8, service.getMaxRetries());
                   ret.setBoolean(9, service.isThreadable());
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
                "bool_threadable = ? " +
            "WHERE " +
                "pk_service = ?";

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
                     ret.setArray(2, conn.createArrayOf("text", new String[] { }));
                }

                ret.setInt(3, service.getMinRam());
                ret.setInt(4, service.getMaxRam());
                ret.setInt(5, service.getMinCores());
                ret.setInt(6, service.getMaxCores());
                ret.setInt(7, service.getMaxRetries());
                ret.setBoolean(8, service.isThreadable());
                ret.setObject(9, UUID.fromString(service.id));
                return ret;
            }
        }) == 1;
    }

    @Override
    public boolean delete(UUID id) {
        return jdbc.update("DELETE FROM plow.service WHERE pk_service=?", id) == 1;
    }
}
