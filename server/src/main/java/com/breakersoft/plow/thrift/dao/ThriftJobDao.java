package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.OutputT;

public interface ThriftJobDao {

    List<JobT> getJobs(JobFilterT filter);

    JobT getJob(String jobId);

    JobT getRunningJob(String name);

    List<OutputT> getOutputs(UUID jobId);

    JobSpecT getJobSpec(UUID jobId);

}
