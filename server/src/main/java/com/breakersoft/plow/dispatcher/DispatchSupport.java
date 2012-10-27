package com.breakersoft.plow.dispatcher;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.rndaemon.RndClient;

/**
 * Non-transactional dispatcher operations.
 *
 * @author chambers
 *
 */
public class DispatchSupport {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchSupport.class);

    @Autowired
    DispatchService dispatcherService;

    public void runRndTask(RunTaskCommand cmd, DispatchProc proc) {
        RndClient client = new RndClient(proc.getNodeName(), 11338);
        client.runProcess(cmd);
    }

    public boolean canDispatch(DispatchTask task, DispatchProc proc) {

        if (proc.getMemory() < task.getMinMemory()) {
            return false;
        }

        if (proc.getCores() < task.getMinCores()) {
            return false;
        }

        return true;
    }

    public boolean canDispatch(DispatchTask task, DispatchNode node) {

        if (!node.isDispatchable()) {
            return false;
        }

        if (node.getMemory() < task.getMinMemory()) {
            return false;
        }

        if (node.getCores() < task.getMinCores()) {
            return false;
        }

        return true;
    }
}
