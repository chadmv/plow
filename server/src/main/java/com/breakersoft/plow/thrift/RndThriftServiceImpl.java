package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.scheduler.SchedulerEventHandler;


@ThriftService
public class RndThriftServiceImpl implements RndServiceApi.Iface {

    @Autowired
    SchedulerEventHandler handler;

    @Override
    public void sendPing(Ping ping) throws RndException {
        handler.handleNodePing(ping);
    }

    @Override
    public void taskComplete(RunTaskResult result) throws RndException {
        handler.handleRunTaskResult(result);
    }
}
