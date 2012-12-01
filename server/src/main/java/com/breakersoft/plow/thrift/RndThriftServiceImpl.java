package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.BackEndDispatcher;
import com.breakersoft.plow.dispatcher.FrontEndDispatcher;
import com.breakersoft.plow.dispatcher.PingHandler;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;
import com.breakersoft.plow.service.NodeService;

@ThriftService
public class RndThriftServiceImpl implements RndServiceApi.Iface {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndThriftServiceImpl.class);

    @Autowired
    PingHandler pingHandler;

    @Autowired
    BackEndDispatcher backEndDispatcher;

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
