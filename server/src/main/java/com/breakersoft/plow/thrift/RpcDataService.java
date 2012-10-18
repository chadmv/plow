package com.breakersoft.plow.thrift;

import java.util.List;

public interface RpcDataService {

    public List<JobT> getJobs(JobFilter filter);
}
