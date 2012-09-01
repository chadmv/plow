package com.breakersoft.plow.dao.pgsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.JobE;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.AbstractDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.thrift.JobState;
import com.breakersoft.plow.util.JdbcUtils;

@Repository
public final class JobDaoImpl extends AbstractDao implements JobDao {

	public static final RowMapper<Job> MAPPER = new RowMapper<Job>() {

	    @Override
	    public Job mapRow(ResultSet rs, int rowNum)
	            throws SQLException {
	    	JobE job = new JobE();
	    	job.setJobId(UUID.fromString(rs.getString(1)));
	    	job.setProjectId(UUID.fromString(rs.getString(2)));
	    	return job;
	    }
	};
	
	private static final String GET =
			"SELECT " +
				"pk_job,"+
				"pk_project " +
			"FROM " +
				"plow.job ";
	
	@Override
	public Job get(String name, JobState state) {
		return jdbc.queryForObject(
				GET + "WHERE str_name=? AND int_state=?",
				MAPPER, name, state.ordinal());
	}
	
	@Override
	public Job get(UUID id) {
		return jdbc.queryForObject(
				GET + "WHERE pk_job=?",
				MAPPER, id);
	}
	
	private static final String INSERT[] = {
		JdbcUtils.Insert("plow.job",
				"pk_job", "pk_project", "str_name", "str_active_name",
				"str_user", "int_uid", "int_state", "bool_paused"),
	};
	
	@Override
	public Job create(Project project, Blueprint blueprint) {
		
		final UUID jobId = UUID.randomUUID();
		jdbc.update(
				INSERT[0],
				jobId,
				project.getProjectId(),
				blueprint.getFullJobName(),
				blueprint.getFullJobName(),
				blueprint.getUsername(),
				blueprint.getUid(),
				JobState.INITIALIZE.ordinal(),
				blueprint.isPaused());

		final JobE job = new JobE();
		job.setJobId(jobId);
		job.setProjectId(project.getProjectId());
		job.setFolderId(null); // Don't know folder yet
		return job;
	}
}
