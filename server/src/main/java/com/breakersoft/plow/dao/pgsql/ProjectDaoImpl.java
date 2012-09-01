package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.ProjectE;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public final class ProjectDaoImpl extends AbstractDao implements ProjectDao {

	public static final RowMapper<Project> MAPPER = new RowMapper<Project>() {

	    @Override
	    public Project mapRow(ResultSet rs, int rowNum)
	            throws SQLException {
	
	    	ProjectE project = new ProjectE();
	    	project.setProjectId(UUID.fromString(rs.getString(1)));
	    	return project;
	    }
	};
	
	@Override
	public Project create(String name, String title) {
		
		final UUID projectId = UUID.randomUUID();
		jdbc.update(
				JdbcUtils.Insert("plow.project", 
						"pk_project", "str_name", "str_title"),
				projectId, name, title);
		
		final ProjectE project = new ProjectE();
		project.setProjectId(projectId);
		return project;
	}

	@Override
	public Project get(String name) {
		return jdbc.queryForObject(
				"SELECT pk_project FROM plow.project WHERE str_name=?",
				MAPPER, name);
	}
}
