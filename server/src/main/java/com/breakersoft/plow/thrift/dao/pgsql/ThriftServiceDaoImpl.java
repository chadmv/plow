package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.ServiceT;
import com.breakersoft.plow.thrift.dao.ThriftServiceDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
@Transactional(readOnly=true)
public class ThriftServiceDaoImpl extends AbstractDao implements ThriftServiceDao {

    public static final RowMapper<ServiceT> MAPPER = new RowMapper<ServiceT>() {

        @Override
        public ServiceT mapRow(ResultSet rs, int rowNum) throws SQLException {

            final ServiceT service = new ServiceT();
            service.setId(rs.getString("pk_service"));
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

    private static final String GET =
            "SELECT " +
                "pk_service,"+
                "str_name,"+
                "int_cores_max,"+
                "int_cores_min,"+
                "int_ram_min,"+
                "int_ram_max,"+
                "int_retries_max, "+
                "str_tags, " +
                "bool_threadable," +
                "isset_int_cores_min,"+
                "isset_int_cores_max,"+
                "isset_int_ram_min,"+
                "isset_int_ram_max,"+
                "isset_str_tags,"+
                "isset_bool_threadable,"+
                "isset_int_retries_max " +
            "FROM " +
                "plow.service ";

    @Override
    public ServiceT getService(String name) {
        return jdbc.queryForObject(GET + " WHERE str_name=?", MAPPER, name);
    }

    @Override
    public ServiceT getService(UUID id) {
        return jdbc.queryForObject(GET + " WHERE pk_service=?", MAPPER, id);
    }

    @Override
    public List<ServiceT> getServices() {
        return jdbc.query(GET, MAPPER);
    }
}
