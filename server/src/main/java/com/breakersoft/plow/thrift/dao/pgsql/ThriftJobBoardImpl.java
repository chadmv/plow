package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import com.breakersoft.plow.thrift.TaskTotalsT;
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
                "job.int_state = ? " +
            "AND " +
                "job.pk_project=?";

    private static final String GET_FOLDERS =
            "SELECT " +
                "folder.pk_folder,"+
                "folder.str_name, "+
                "folder.int_order, " +
                "folder_dsp.int_max_cores, " +
                "folder_dsp.int_min_cores " +
            "FROM " +
                "folder " +
            "INNER JOIN " +
                "folder_dsp " +
            "ON " +
                "folder.pk_folder = folder_dsp.pk_folder " +
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
                folder.setJobs(new ArrayList<JobT>());
                folder.setTotals(new TaskTotalsT());
                folder.setMaxCores(rs.getInt("int_max_cores"));
                folder.setMinCores(rs.getInt("int_min_cores"));

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
                job.setFolderId(rs.getString("pk_folder"));
                job.setName(rs.getString("str_name"));
                job.setUid(rs.getInt("int_uid"));
                job.setUsername(rs.getString("str_username"));
                job.setPaused(rs.getBoolean("bool_paused"));
                job.setRunCores(rs.getInt("int_run_cores"));
                job.setMaxCores(rs.getInt("int_max_cores"));
                job.setMinCores(rs.getInt("int_min_cores"));
                job.setStartTime(rs.getLong("time_started"));
                job.setStopTime(rs.getLong("time_stopped"));
                job.setState(JobState.findByValue(rs.getInt("int_state")));
                job.setTotals(JdbcUtils.getTaskTotals(rs));

                folder.totals.deadTaskCount += job.totals.deadTaskCount;
                folder.totals.dependTaskCount += job.totals.deadTaskCount;
                folder.totals.eatenTaskCount += job.totals.eatenTaskCount;
                folder.totals.runningTaskCount += job.totals.runningTaskCount;
                folder.totals.succeededTaskCount += job.totals.succeededTaskCount;
                folder.totals.totalTaskCount += job.totals.totalTaskCount;
                folder.totals.waitingTaskCount += job.totals.waitingTaskCount;
                folder.runCores += job.runCores;

                folder.jobs.add(job);
            }
        }, JobState.RUNNING.ordinal(), projectId);

        Collections.sort(result, new Comparator<FolderT>() {
            @Override
            public int compare(FolderT o1, FolderT o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        return result;
    }
}
