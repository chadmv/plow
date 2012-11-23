package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.OutputT;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;

@Repository
@Transactional(readOnly = true)
public class ThriftJobDaoImpl extends AbstractDao implements ThriftJobDao {

    public static final RowMapper<JobT> MAPPER = new RowMapper<JobT>() {

        @Override
        public JobT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            JobT job = new JobT();
            job.id = rs.getString("pk_job");
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
            return job;
        }
    };

    private static final String GET =
        "SELECT " +
            "job.pk_job,"+
            "job.pk_folder,"+
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
        "INNER JOIN folder ON job.pk_folder = folder.pk_folder " +
        "INNER JOIN project ON job.pk_project = project.pk_project ";

    @Override
    public List<JobT> getJobs(JobFilterT filter) {

        final List<String> clauses = Lists.newArrayListWithExpectedSize(6);
        final List<Object> values = Lists.newArrayList();

        if (filter.isSetProject() && !filter.project.isEmpty()) {
            clauses.add(JdbcUtils.In(
                    "project.str_name", filter.project.size()));
            values.addAll(filter.project);
        }

        if (filter.isSetUser() && !filter.user.isEmpty()) {
            clauses.add(JdbcUtils.In(
                    "job.str_username", filter.user.size()));
            values.addAll(filter.user);
        }

        if (filter.isSetRegex() && !filter.regex.isEmpty()) {
            clauses.add("str_name ~ ?");
            values.add(filter.regex);
        }

        if (filter.isSetStates() && !filter.states.isEmpty()) {
            clauses.add(JdbcUtils.In(
                    "job.int_state", filter.states.size()));
            for (JobState state: filter.states) {
                values.add(state.ordinal());
            }
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        if (!values.isEmpty()) {
            sb.append(" WHERE " );
            sb.append(StringUtils.join(clauses, " AND "));
        }

        return jdbc.query(sb.toString(), MAPPER, values.toArray());
    }

    private static final String GET_BY_ID =
            GET + " WHERE job.pk_job=?";

    @Override
    public JobT getJob(String jobId) {
        return jdbc.queryForObject(GET_BY_ID, MAPPER, UUID.fromString(jobId));
    }

    private static final String GET_BY_NAME =
            GET + " WHERE job.str_active_name=?";

    @Override
    public JobT getRunningJob(String name) {
        return jdbc.queryForObject(GET_BY_NAME, MAPPER, name);
    }

    @Override
    public List<OutputT> getOutputs(UUID jobId) {
        return jdbc.query("SELECT str_path, attrs FROM plow.output WHERE pk_job=?",
                ThriftLayerDaoImpl.OUT_MAPPER, jobId);
    }
}
