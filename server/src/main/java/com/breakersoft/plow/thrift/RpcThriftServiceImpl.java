package com.breakersoft.plow.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;

@ThriftService
public class RpcThriftServiceImpl implements RpcServiceApi.Iface {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcThriftServiceImpl.class);

    @Autowired
    JobLauncherService jobLauncherService;

    @Autowired
    ThriftJobDao thriftJobDao;

    @Override
    public JobT launch(Blueprint bp) throws PlowException, TException {

        logger.info("launchung job! " + bp);

        JobLaunchEvent event =  jobLauncherService.launch(bp);

        JobT result = new JobT();
        result.id = event.getJob().getJobId().toString();
        result.name = event.getBlueprint().job.getName();
        return result;
    }

    @Override
    public JobT getActiveJob(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JobT getJob(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LayerT getLayer(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LayerT> getLayers(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TaskT getTask(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TaskT> getTasks(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<JobT> getJobs(JobFilter filter) throws PlowException, TException {
        return thriftJobDao.getJobs(filter);
    }

}
