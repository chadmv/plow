package com.breakersoft.plow.dispatcher;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;

@Component
public class PingHandler {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(PingHandler.class);

    @Autowired
    NodeService nodeService;

    @Autowired
    JobService jobService;

    @Autowired
    FrontEndDispatcher frontEndDispatcher;

    @Autowired
    BackEndDispatcher backEndDispatcher;

    public void handlePing(Ping ping) {

        Node node;
        try {
            node = nodeService.getNode(ping.hostname);
            nodeService.updateNode(node, ping);
        } catch (EmptyResultDataAccessException e) {
            node = nodeService.createNode(ping);
        }

        jobService.updateRunningTasks(ping.tasks);

        logger.info("{} node reporting in.", node.getName());
        frontEndDispatcher.book(node);
    }

}