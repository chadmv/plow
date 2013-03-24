package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.ProjectT;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;

@Repository
@Transactional(readOnly=true)
public class ThriftProjectDaoImpl extends AbstractDao implements ThriftProjectDao {

    public static final RowMapper<ProjectT> MAPPER = new RowMapper<ProjectT>() {

        @Override
        public ProjectT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ProjectT project = new ProjectT();
            project.setId(rs.getString("pk_project"));
            project.setCode(rs.getString("str_code"));
            project.setTitle(rs.getString("str_title"));
            project.setIsActive(rs.getBoolean("bool_active"));
            return project;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_project,"+
                "str_code,"+
                "str_title, "+
                "bool_active " +
            "FROM " +
                "project ";

    @Override
    public ProjectT get(UUID id) {
        return jdbc.queryForObject(GET + " WHERE pk_project=?", MAPPER, id);
    }

    @Override
    public ProjectT get(String name) {
        return jdbc.queryForObject(GET + " WHERE str_code=?", MAPPER, name);
    }

    @Override
    public List<ProjectT> all() {
        return jdbc.query(GET, MAPPER);
    }

    @Override
    public List<ProjectT> active() {
        return jdbc.query(GET + "WHERE bool_active='t'", MAPPER);
    }

    @Override
    public long getPlowTime() {
        return jdbc.queryForLong("SELECT currentTimeMillis()");
    }
}
