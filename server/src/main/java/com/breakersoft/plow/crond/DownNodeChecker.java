package com.breakersoft.plow.crond;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.thrift.NodeState;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;

public class DownNodeChecker extends AbstractCrondTask {

    private static final Logger logger = LoggerFactory.getLogger(DownNodeChecker.class);

    @Autowired
    private NodeService nodeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private DispatchService dispatchService;

    public DownNodeChecker() {
        super(CrondTask.DOWN_NODE_CHECK);
    }

    @Override
    protected void run() {

        logger.info("Running check for down nodes.");

        final List<Node> nodes = nodeService.getUnresponsiveNodes();
        if (nodes.isEmpty()) {
            return;
        }

        logger.info("Found {} unresponsive nodes, flipping to down state.", nodes.size());
        for (Node node: nodes) {

            logger.info("{} is down.", node);

            if (!nodeService.setNodeState(node, NodeState.DOWN)) {
                // Something else set the state to down.
                continue;
            }

            TaskFilterT filter = new TaskFilterT();
            filter.addToNodeIds(node.getNodeId().toString());

            // Now stop all the tasks running on this node.
            for (Task task: jobService.getTasks(filter)) {

                if (dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.NODE_DOWN)) {

                    Proc proc = nodeService.getProc(task);
                    dispatchService.deallocateProc(proc, "Node is unresponsive.");

                    // Might want to try to actually kill the process here just in case.
                }
            }
        }
    }
}
