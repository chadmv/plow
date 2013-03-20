package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.TaskTotalsT;
import com.breakersoft.plow.thrift.dao.ThriftFolderDao;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
@Transactional(readOnly=true)
public class ThriftFolderDaoImpl extends AbstractDao implements ThriftFolderDao {

    private static final  RowMapper<FolderT> MAPPER = new RowMapper<FolderT>() {
         @Override
         public FolderT mapRow(ResultSet rs, int rowNum) throws SQLException {
             FolderT folder = new FolderT();
             folder.setId(rs.getString("pk_folder"));
             folder.setOrder(rs.getInt("int_order"));
             folder.setName(rs.getString("str_name"));
             folder.setMinCores(rs.getInt("int_min_cores"));
             folder.setMaxCores(rs.getInt("int_max_cores"));
             folder.setRunCores(rs.getInt("int_run_cores"));
             return folder;
         }
    };

    private static final  RowMapper<TaskTotalsT> TOTALS_MAPPER = new RowMapper<TaskTotalsT>() {
        @Override
        public TaskTotalsT mapRow(ResultSet rs, int rowNum) throws SQLException {
            return JdbcUtils.getTaskTotals(rs);
         }
    };

    private static final String GET_TOTALS =
        "SELECT " +
            "folder.pk_folder,"+
            "folder.int_order, "+
            "SUM(job_count.int_total) AS int_total," +
            "SUM(job_count.int_succeeded) AS int_succeeded, " +
            "SUM(job_count.int_running) AS int_running, " +
            "SUM(job_count.int_dead) AS int_dead," +
            "SUM(job_count.int_eaten) AS int_eaten," +
            "SUM(job_count.int_waiting) AS int_waiting,"+
            "SUM(job_count.int_depend) AS int_depend " +
        "FROM " +
            "folder " +
        "LEFT JOIN job ON folder.pk_folder = job.pk_folder " +
        "LEFT JOIN job_count ON job.pk_job = job_count.pk_job ";

    private static final String GET =
        "SELECT " +
            "folder.pk_folder,"+
            "folder.str_name, "+
            "folder.int_order, " +
            "folder_dsp.int_min_cores,"+
            "folder_dsp.int_max_cores,"+
            "folder_dsp.int_run_cores " +
        "FROM " +
            "folder " +
        "INNER JOIN folder_dsp ON folder.pk_folder = folder_dsp.pk_folder ";

    private static final String GET_BY_ID =
            GET + " WHERE folder.pk_folder=?";

    private static final String GET_TOTALS_BY_ID =
            GET_TOTALS + " WHERE folder.pk_folder=? GROUP BY folder.pk_folder, folder.int_order";

    @Override
    public FolderT get(UUID id){
        FolderT folder = jdbc.queryForObject(GET_BY_ID, MAPPER, id);
        folder.setTotals(jdbc.queryForObject(GET_TOTALS_BY_ID, TOTALS_MAPPER, id));
        return folder;
    }

    private static final String GET_BY_PROJ =
            GET + " WHERE folder.pk_project=? ORDER BY folder.int_order ASC";

    private static final String GET_TOTALS_BY_PROJ =
            GET_TOTALS + " WHERE folder.pk_project=? GROUP BY folder.pk_folder, folder.int_order ORDER BY folder.int_order ASC ";

    @Override
    public List<FolderT> getFolders(Project project) {
         List<FolderT> result = jdbc.query(GET_BY_PROJ, MAPPER, project.getProjectId());
         List<TaskTotalsT> totals = jdbc.query(GET_TOTALS_BY_PROJ, TOTALS_MAPPER, project.getProjectId());
         for (int i=0; i<result.size(); i++) {
             result.get(i).setTotals(totals.get(i));
         }
         return result;
    }
}
