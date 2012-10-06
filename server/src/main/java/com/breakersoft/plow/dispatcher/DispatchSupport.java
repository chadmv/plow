package com.breakersoft.plow.dispatcher;

import java.util.Arrays;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.thrift.RndClient;
import com.google.common.collect.Maps;

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

    public void runRndTask(DispatchTask task, DispatchProc proc) {

        RunTaskCommand cmd = new RunTaskCommand();
        cmd.command = Arrays.asList(task.getCommand());
        cmd.cores = proc.getCores();
        cmd.env = Maps.newHashMap();
        cmd.taskId = task.getTaskId().toString();
        cmd.procId = proc.getProcId().toString();
        cmd.jobId = task.getJobId().toString();
        cmd.logFile = String.format("/tmp/%s.log", task.getName());

        RndClient client = new RndClient(proc.getNodeName(), 11338);
        client.runProcess(cmd);
        logger.info("process is running");
    }

    public boolean canDispatch(DispatchTask task, DispatchNode node) {

        if (node.getMemory() < task.getMinMemory()) {
            return false;
        }

        if (node.getCores() < task.getMinCores()) {
            return false;
        }

        return true;
    }
}
