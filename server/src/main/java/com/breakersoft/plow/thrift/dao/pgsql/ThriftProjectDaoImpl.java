package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.ProjectT;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;

@Repository
public class ThriftProjectDaoImpl extends AbstractDao implements ThriftProjectDao {

    public static final RowMapper<ProjectT> MAPPER = new RowMapper<ProjectT>() {

        @Override
        public ProjectT mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            ProjectT project = new ProjectT();
            project.setId(rs.getString("pk_project"));
            project.setName(rs.getString("str_name"));
            project.setTitle(rs.getString("str_title"));
            return project;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_project,"+
                "str_name,"+
                "str_title "+
            "FROM " +
                "project ";

    @Override
    public ProjectT get(UUID id) {
        return jdbc.queryForObject(GET + " WHERE pk_project=?", MAPPER, id);
    }

    @Override
    public ProjectT get(String name) {
        return jdbc.queryForObject(GET + " WHERE str_name=?", MAPPER, name);
    }
}
