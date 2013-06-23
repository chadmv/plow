package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.FolderT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.JobStatsT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.TaskTotalsT;
import com.breakersoft.plow.thrift.dao.ThriftJobBoardDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Repository
@Transactional(readOnly=true)
public class ThriftJobBoardImpl extends AbstractDao implements ThriftJobBoardDao {

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
                "job_dsp.int_cores_max,"+
                "job_dsp.int_cores_min,"+
                "job_dsp.int_cores_run, " +
                "job_count.int_total, "+
                "job_count.int_succeeded,"+
                "job_count.int_running,"+
                "job_count.int_dead,"+
                "job_count.int_eaten,"+
                "job_count.int_waiting,"+
                "job_count.int_depend, "+
                "job_stat.int_ram_high, "+
                "job_stat.flt_cores_high, "+
                "job_stat.int_core_time_high, "+
                "job_stat.int_total_core_time_success, "+
                "job_stat.int_total_core_time_fail, "+
                "job_stat.int_clock_time_high "+
            "FROM " +
                "job " +
            "INNER JOIN " +
                "job_dsp ON job.pk_job = job_dsp.pk_job " +
            "INNER JOIN " +
                "job_count ON job.pk_job = job_count.pk_job " +
            "INNER JOIN " +
                "job_stat ON job.pk_job = job_stat.pk_job " +
            "WHERE " +
                "job.int_state = ? " +
            "AND " +
                "job.pk_project = ?";

    private static final String GET_FOLDERS =
            "SELECT " +
                "folder.pk_folder,"+
                "folder.str_name, "+
                "folder.int_order, " +
                "folder_dsp.int_cores_max, " +
                "folder_dsp.int_cores_min, " +
                "folder_dsp.int_cores_run  " +
            "FROM " +
                "folder " +
            "INNER JOIN folder_dsp ON folder.pk_folder = folder_dsp.pk_folder " +
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
                folder.setMaxCores(rs.getInt("int_cores_max"));
                folder.setMinCores(rs.getInt("int_cores_min"));
                folder.setRunCores(rs.getInt("int_cores_run"));

                result.add(folder);
                folders.put(folder.getId(), folder);

            }
        }, projectId);

        jdbc.query(GET_JOBS, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {

                final FolderT folder = folders.get(rs.getString("pk_folder"));

                JobStatsT stats = new JobStatsT();
                stats.highRam = rs.getInt("int_ram_high");
                stats.highCores = rs.getDouble("flt_cores_high");
                stats.highCoreTime = rs.getInt("int_core_time_high");
                stats.totalSuccessCoreTime = rs.getLong("int_total_core_time_success");
                stats.totalFailCoreTime = rs.getLong("int_total_core_time_fail");
                stats.highClockTime = rs.getLong("int_clock_time_high");
                stats.totalCoreTime = stats.totalSuccessCoreTime + stats.totalFailCoreTime;

                final JobT job = new JobT();
                job.id = rs.getString("pk_job");
                job.setFolderId(rs.getString("pk_folder"));
                job.setName(rs.getString("str_name"));
                job.setUid(rs.getInt("int_uid"));
                job.setUsername(rs.getString("str_username"));
                job.setPaused(rs.getBoolean("bool_paused"));
                job.setRunCores(rs.getInt("int_cores_run"));
                job.setMaxCores(rs.getInt("int_cores_max"));
                job.setMinCores(rs.getInt("int_cores_min"));
                job.setStartTime(rs.getLong("time_started"));
                job.setStopTime(rs.getLong("time_stopped"));
                job.setState(JobState.findByValue(rs.getInt("int_state")));

                job.setTotals(JdbcUtils.getTaskTotals(rs));
                job.setStats(stats);

                folder.totals.deadTaskCount += job.totals.deadTaskCount;
                folder.totals.dependTaskCount += job.totals.dependTaskCount;
                folder.totals.eatenTaskCount += job.totals.eatenTaskCount;
                folder.totals.runningTaskCount += job.totals.runningTaskCount;
                folder.totals.succeededTaskCount += job.totals.succeededTaskCount;
                folder.totals.totalTaskCount += job.totals.totalTaskCount;
                folder.totals.waitingTaskCount += job.totals.waitingTaskCount;

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
