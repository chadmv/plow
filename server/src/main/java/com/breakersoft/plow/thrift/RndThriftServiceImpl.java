package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.PingHandler;
import com.breakersoft.plow.dispatcher.TaskCompleteHandler;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;

@ThriftService
public class RndThriftServiceImpl implements RndServiceApi.Iface {

    @Autowired
    PingHandler pingHandler;

    @Autowired
    TaskCompleteHandler backEndDispatcher;

    @Override
    public void sendPing(Ping ping) throws RndException, TException {
        pingHandler.handlePing(ping);
    }

    @Override
    public void taskComplete(RunTaskResult result) throws RndException,
            TException {
        backEndDispatcher.taskComplete(result);
    }

}
