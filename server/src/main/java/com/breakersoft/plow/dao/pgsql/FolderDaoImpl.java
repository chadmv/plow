package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FolderE;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.FolderDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public class FolderDaoImpl extends AbstractDao implements FolderDao {

    public static final RowMapper<Folder> MAPPER = new RowMapper<Folder>() {
        @Override
        public Folder mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            FolderE folder = new FolderE();
            folder.setFolderId((UUID)rs.getObject(1));
            folder.setProjectId((UUID)rs.getObject(2));
            folder.setName(rs.getString(3));
            return folder;
        }
    };

    private static final String GET =
            "SELECT " +
                "pk_folder,"+
                "pk_project, " +
                "str_name " +
            "FROM " +
                "plow.folder " +
            "WHERE " +
                "pk_folder = ?";

    @Override
    public Folder get(UUID id) {
        return jdbc.queryForObject(GET, MAPPER, id);
    }

    private static final String GET_DEFAULT =
            "SELECT " +
                "folder.pk_folder, "+
                "folder.pk_project," +
                "folder.str_name " +
            "FROM " +
                "plow.folder, " +
                "plow.project " +
            "WHERE " +
                "folder.pk_folder = project.pk_folder_default " +
            "AND " +
                "project.pk_project = ?";

    @Override
    public Folder getDefaultFolder(Project project) {
        return jdbc.queryForObject(GET_DEFAULT, MAPPER, project.getProjectId());
    }

    private static final String INSERT =
            JdbcUtils.Insert("plow.folder",
                "pk_folder",
                "pk_project",
                "int_order",
                "str_name");

    @Override
    public Folder createFolder(Project project, String name) {
        UUID id = UUID.randomUUID();

        // Block out other threads from adding folders.
        jdbc.queryForObject("SELECT pk_project FROM plow.project WHERE pk_project=? FOR UPDATE",
                String.class, project.getProjectId());

        jdbc.update(INSERT, id, project.getProjectId(), 32000, name);
        jdbc.update("INSERT INTO plow.folder_dsp (pk_folder) VALUES (?)", id);

        renumber(project);
        return get(id);
    }

    public void renumber(Project project) {
        int order = 0;
        for(Map<String,Object> item:
            jdbc.queryForList("SELECT pk_folder FROM folder WHERE pk_project=? ORDER BY int_order",
                project.getProjectId())) {
            jdbc.update("UPDATE folder SET int_order=? WHERE pk_folder=?",
                    order, item.get("pk_folder"));
            order+=2;
        }
    }

    @Override
    public void setMaxCores(Folder folder, int value) {
        jdbc.update("UPDATE plow.folder_dsp SET int_max_cores=? WHERE pk_folder=?",
                value, folder.getFolderId());
    }

    @Override
    public void setMinCores(Folder folder, int value) {
        jdbc.update("UPDATE plow.folder_dsp SET int_min_cores=? WHERE pk_folder=?",
                value, folder.getFolderId());
    }

    @Override
    public void setName(Folder folder, String name) {
        jdbc.update("UPDATE plow.folder SET str_name=? WHERE pk_folder=?",
                name, folder.getFolderId());
    }

    @Override
    public void delete(Folder folder) {
        jdbc.update("DELETE FROM plow.folder WHERE pk_folder=?", folder.getFolderId());
    }

}
