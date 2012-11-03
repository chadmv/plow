package com.breakersoft.plow.thrift.dao;

import java.util.List;

import com.breakersoft.plow.thrift.JobFilterT;
import com.breakersoft.plow.thrift.JobT;

public interface ThriftJobDao {

    List<JobT> getJobs(JobFilterT filter);

    JobT getJob(String jobId);

    JobT getRunningJob(String name);

}
