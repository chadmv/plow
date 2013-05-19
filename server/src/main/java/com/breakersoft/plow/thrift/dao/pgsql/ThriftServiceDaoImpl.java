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
            service.id = rs.getString("pk_service");
            service.name = rs.getString("str_name");
            service.maxCores = rs.getInt("int_cores_max");
            service.minCores = rs.getInt("int_cores_min");
            service.minRam = rs.getInt("int_ram_min");
            service.maxRam = rs.getInt("int_ram_max");
            service.maxRetries = rs.getInt("int_retries_max");
            service.tags = JdbcUtils.toList(rs.getArray("str_tags"));
            service.threadable = rs.getBoolean("bool_threadable");
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
                "bool_threadable " +
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
