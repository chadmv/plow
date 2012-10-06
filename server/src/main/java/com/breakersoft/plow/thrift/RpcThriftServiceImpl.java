package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobLauncherService;

@ThriftService
public class RpcThriftServiceImpl implements RpcServiceApi.Iface {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcThriftServiceImpl.class);

    @Autowired
    JobLauncherService jobLauncherService;

    @Override
    public JobT launch(JobBp bp) throws PlowException, TException {

        logger.info("launchung job! " + bp);

        JobLaunchEvent event =  jobLauncherService.launch(bp);

        JobT result = new JobT();
        result.id = event.getJob().getJobId().toString();
        result.name = event.getBlueprint().getName();
        return result;
    }

}
