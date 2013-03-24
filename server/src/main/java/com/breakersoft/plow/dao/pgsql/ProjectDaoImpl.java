package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Folder;
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

    private static final String INSERT =
            JdbcUtils.Insert("plow.project",
                    "pk_project",
                    "str_code",
                    "str_title");

    @Override
    public Project create(String title, String code) {
        final UUID projectId = UUID.randomUUID();
        jdbc.update(INSERT, projectId, code, title);

        final ProjectE project = new ProjectE();
        project.setProjectId(projectId);
        return project;
    }

    @Override
    public Project get(String code) {
        return jdbc.queryForObject(
                "SELECT pk_project FROM plow.project WHERE str_code=?",
                MAPPER, code);
    }

    @Override
    public Project get(UUID id) {
        return jdbc.queryForObject(
                "SELECT pk_project FROM plow.project WHERE pk_project=?",
                MAPPER, id);
    }

    @Override
    public List<Project> getAll() {
        return jdbc.query(
                "SELECT pk_project FROM plow.project",
                MAPPER);
    }

    @Override
    public void setDefaultFolder(Project project, Folder folder) {
        jdbc.update("UPDATE plow.project SET pk_folder_default=? WHERE pk_project=?",
                folder.getFolderId(), project.getProjectId());
    }

    @Override
    public void setActive(Project project, boolean active) {
        jdbc.update("UPDATE plow.project SET bool_active=? WHERE pk_project=?",
        		active, project.getProjectId());
    }
}
