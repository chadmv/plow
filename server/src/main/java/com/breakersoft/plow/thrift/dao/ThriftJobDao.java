package com.breakersoft.plow.thrift.dao;

import java.util.List;

import com.breakersoft.plow.thrift.JobFilter;
import com.breakersoft.plow.thrift.JobT;

public interface ThriftJobDao {

    List<JobT> getJobs(JobFilter filter);

    JobT getJob(String jobId);

}
