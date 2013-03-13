package com.breakersoft.plow.dispatcher.domain;

import java.util.UUID;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.JobId;

public class DispatchJob implements JobId {

	private UUID jobId;

	public DispatchJob() { }
	public DispatchJob(Job job) {
		this.jobId = job.getJobId();
	}

	public UUID getJobId() {
		return jobId;
	}

	public void setJobId(UUID jobId) {
		this.jobId = jobId;
	}
}
