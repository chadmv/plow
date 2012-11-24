package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.TaskCounts;
import com.breakersoft.plow.thrift.dao.ThriftJobBoardDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Repository
public class ThriftJobBoardImpl extends AbstractDao implements ThriftJobBoardDao {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(ThriftJobBoardImpl.class);

    private static final String GET_JOBS =
        "SELECT " +
                "job.pk_job,"+
                "job.pk_folder," +
                "job.str_name,"+
                "job.bool_paused,"+
                "job.int_uid,"+
                "job.str_username,"+
                "job.int_state,"+
                "job.time_started,"+
                "job.time_stopped,"+
                "job_dsp.int_max_cores,"+
                "job_dsp.int_min_cores,"+
                "job_dsp.int_run_cores, " +
                "job_count.int_total, "+
                "job_count.int_succeeded,"+
                "job_count.int_running,"+
                "job_count.int_dead,"+
                "job_count.int_eaten,"+
                "job_count.int_waiting,"+
                "job_count.int_depend "+
            "FROM " +
                "job " +
            "INNER JOIN job_dsp ON job.pk_job = job_dsp.pk_job " +
            "INNER JOIN job_count ON job.pk_job = job_count.pk_job " +
            "WHERE " +
                "job.pk_project=?";

    private static final String GET_FOLDERS =
            "SELECT " +
                "folder.pk_folder,"+
                "folder.str_name, "+
                "folder.int_order " +
            "FROM " +
                "folder " +
            "WHERE " +
                "folder.pk_project = ? " +
            "ORDER BY " +
                "int_order ASC ";

    @Override
    public List<FolderT> getJobBoard(UUID projectId) {
        final List<FolderT> result = Lists.newArrayList();
        final Map<String, FolderT> folders = Maps.newHashMap();

        jdbc.query(GET_FOLDERS, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                // Empty folder
                FolderT folder = new FolderT();
                folder.setId(rs.getString("pk_folder"));
                folder.setOrder(rs.getInt("int_order"));
                folder.setName(rs.getString("str_name"));
                folder.jobs = Lists.newArrayList();

                result.add(folder);
                folders.put(folder.getId(), folder);

            }
        }, projectId);

        jdbc.query(GET_JOBS, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {

                FolderT folder = folders.get(rs.getString("pk_folder"));

                JobT job = new JobT();
                job.id = rs.getString("pk_job");
                job.folderId = rs.getString("pk_folder");
                job.name = rs.getString("str_name");
                job.uid = rs.getInt("int_uid");
                job.username = rs.getString("str_username");
                job.paused = rs.getBoolean("bool_paused");
                job.maxCores = rs.getInt("int_max_cores");
                job.minCores = rs.getInt("int_min_cores");
                job.startTime = rs.getLong("time_started");
                job.stopTime = rs.getLong("time_stopped");
                job.state = JobState.findByValue(rs.getInt("int_state"));
                job.setTotals(JdbcUtils.getTaskTotals(rs));

                folder.jobs.add(job);
            }
        }, projectId);

        Collections.sort(result, new Comparator<FolderT>() {
            @Override
            public int compare(FolderT o1, FolderT o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return result;
    }
}
