package com.breakersoft.plow.dispatcher;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.pipeline.PipelineCommandService;
import com.breakersoft.plow.dispatcher.pipeline.PipelineController;
import com.breakersoft.plow.dispatcher.pipeline.StopTaskCommand;
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.util.PlowUtils;

/**
 * Manages the the running procs on a node.
 *
 * @author chambers
 *
 */
@Component
public class RndEventHandler {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(RndEventHandler.class);

    @Autowired
    StatsService statsService;

    @Autowired
    NodeService nodeService;

    @Autowired
    NodeDispatcher nodeDispatcher;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    JobService jobService;

    @Autowired
    PipelineCommandService pipelineCommandService;

    @Autowired
    PipelineController pipelineController;

    /**
     * This is a oneway method, the RNDaemon is not listening for a response.
     *
     * @param ping
     */
    public void handleNodePing(Ping ping) {

        logger.trace("{} node reporting in.", ping.getHostname());

        DispatchNode node;
        try {
            node = dispatchService.getDispatchNode(ping.hostname);
            nodeService.updateNode(node, ping);
        } catch (EmptyResultDataAccessException e) {
            Node newNode = nodeService.createNode(ping);
            node = dispatchService.getDispatchNode(newNode.getName());
        }

        PlowStats.rndPingCount.incrementAndGet();

        if (PlowUtils.isValid(ping.tasks)) {
            statsService.updateProcRuntimeStats(ping.tasks);
            statsService.updateTaskRuntimeStats(ping.tasks);
        }

        if (node.isDispatchable()) {
            nodeDispatcher.asyncDispatch(node);
        }
    }

    public void handleRunTaskResult(RunTaskResult result) {

        Task task = null;
        DispatchProc proc = null;
        try {
            task = jobService.getTask(UUID.fromString(result.taskId));
            proc = dispatchService.getDispatchProc(result.procId);
        }
        catch (EmptyResultDataAccessException e) {
            logger.error("Bad task complete ping Job:" + result.jobId + ", Task: " + result.taskId, e);
            return;
        }

        PlowStats.rndTaskComplete.incrementAndGet();

        // May throw back to RND if the job's pipeline is full.
        // Will internally spawn a dispatch proc command.
        pipelineController.execute(
                new StopTaskCommand(result, task, proc, pipelineCommandService));
    }
}
