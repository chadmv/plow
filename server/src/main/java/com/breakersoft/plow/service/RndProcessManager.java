package com.breakersoft.plow.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.rndaemon.RndClient;
import com.breakersoft.plow.rndaemon.RndClientPool;

/**
 * Component for handling operations that require talking to
 * multiple render nodes.
 *
 * @author chambers
 *
 */
@Component
public class RndProcessManager {

    private static final Logger logger = LoggerFactory.getLogger(RndProcessManager.class);

    @Autowired
    NodeService nodeService;

    @Autowired
    RndClientPool rndClientPool;

    public void killProcs(Job job, String reason) {
        final List<Proc> procs = nodeService.getProcs(job);

        for (Proc proc: procs) {
            nodeService.setProcUnbooked(proc, true);

            RndClient client = rndClientPool.get(proc.getHostname());
            try {
                client.kill(proc, reason);
            }
            catch (RndClientExecuteException e) {
                logger.warn("Failed to kill process {} on {} ({}), {}",
                        new Object[] { proc.getProcId(), proc.getHostname(), reason, e });
            }
        }
    }
}
