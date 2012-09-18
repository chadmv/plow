package com.breakersoft.plow.rnd;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.ProcessResult;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;
import com.breakersoft.plow.service.NodeService;

public class RndThriftServiceImpl implements RndServiceApi.Iface {

    @Autowired
    NodeService nodeService;

    @Override
    public void sendPing(Ping ping) throws RndException, TException {

        Node node;
        try {
            node = nodeService.getNode(ping.hostname);
            nodeService.updateNode(node, ping);
        } catch (EmptyResultDataAccessException e) {
            node = nodeService.createNode(ping);
        }
    }

    @Override
    public void processCompleted(ProcessResult arg0) throws RndException,
            TException {
        // TODO Auto-generated method stub

    }

}
