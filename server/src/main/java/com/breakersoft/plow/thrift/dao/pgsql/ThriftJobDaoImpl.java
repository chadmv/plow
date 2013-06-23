package com.breakersoft.plow.thrift.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.exceptions.JobSpecException;
import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.thrift.JobStatsT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;
import com.breakersoft.plow.util.JdbcUtils;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.collect.Lists;

@Repository
@Transactional(readOnly=true)
public class ThriftJobDaoImpl extends AbstractDao implements ThriftJobDao {

    public static final RowMapper<JobT> MAPPER = new RowMapper<JobT>() {

        @Override
        public JobT mapRow(ResultSet rs, int rowNum)
                throws SQLException {

            final JobStatsT stats = new JobStatsT();
            stats.highRam = rs.getInt("int_ram_high");
            stats.highCores = rs.getDouble("flt_cores_high");
            stats.highCoreTime = rs.getLong("int_core_time_high");
            stats.totalSuccessCoreTime = rs.getLong("int_total_core_time_success");
            stats.totalFailCoreTime = rs.getLong("int_total_core_time_fail");
            stats.totalCoreTime = stats.totalSuccessCoreTime + stats.totalFailCoreTime;
            stats.highClockTime = rs.getLong("int_clock_time_high");

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

            job.setStats(stats);
            job.setTotals(JdbcUtils.getTaskTotals(rs));
            job.setAttrs((Map<String, String>) rs.getObject("hstore_attrs"));

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
            "job.hstore_attrs,"+
            "job_dsp.int_cores_max,"+
            "job_dsp.int_cores_min,"+
            "job_dsp.int_cores_run, " +
            "job_count.int_total, "+
            "job_count.int_succeeded,"+
            "job_count.int_running,"+
            "job_count.int_dead,"+
            "job_count.int_eaten,"+
            "job_count.int_waiting,"+
            "job_count.int_depend,"+
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
        "INNER JOIN " +
            "folder ON job.pk_folder = folder.pk_folder " +
        "INNER JOIN " +
            "project ON job.pk_project = project.pk_project ";

    @Override
    public List<JobT> getJobs(JobFilterT filter) {

        final List<String> clauses = Lists.newArrayListWithExpectedSize(6);
        final List<Object> values = Lists.newArrayList();

        if (PlowUtils.isValid(filter.project)) {
            clauses.add(JdbcUtils.In(
                    "project.str_code", filter.project.size()));
            values.addAll(filter.project);
        }

        if (PlowUtils.isValid(filter.user)) {
            clauses.add(JdbcUtils.In(
                    "job.str_username", filter.user.size()));
            values.addAll(filter.user);
        }

        if (PlowUtils.isValid(filter.name)) {
            clauses.add(JdbcUtils.In(
                    "job.str_name", filter.name.size()));
            values.addAll(filter.name);
        }

        if (PlowUtils.isValid(filter.regex)) {
            clauses.add("job.str_name ~ ?");
            values.add(filter.regex);
        }

        if (PlowUtils.isValid(filter.states)) {
            clauses.add(JdbcUtils.In(
                    "job.int_state", filter.states.size()));
            for (JobState state: filter.states) {
                values.add(state.ordinal());
            }
        }

        if (PlowUtils.isValid(filter.jobIds)) {
            clauses.add(JdbcUtils.In(
                    "job.pk_job", filter.jobIds.size(), "uuid"));
            values.addAll(filter.jobIds);
        }

        final StringBuilder sb = new StringBuilder(512);
        sb.append(GET);
        if (!values.isEmpty()) {
            sb.append(" WHERE " );
            sb.append(StringUtils.join(clauses, " AND "));
        }

        if (filter.matchingOnly && values.isEmpty()) {
            return new ArrayList<JobT>(0);
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
    public JobSpecT getJobSpec(UUID jobId) {

        final String json = jdbc.queryForObject("SELECT str_thrift_spec FROM job_history WHERE pk_job=?",
                String.class, jobId);

        final TDeserializer deserializer = new TDeserializer(new TJSONProtocol.Factory());
        final JobSpecT spec = new JobSpecT();
        try {
            deserializer.deserialize(spec, json.getBytes());
            return spec;
        } catch (TException e) {
            throw new JobSpecException("Failed to parse job spec " + e, e);
        }
    }
}
