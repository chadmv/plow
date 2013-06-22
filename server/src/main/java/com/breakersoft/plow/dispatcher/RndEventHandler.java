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
            statsService.updateLayerRuntimeStats(ping.tasks);
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
        }
        catch (EmptyResultDataAccessException e) {
            logger.error("The task {} no longer exists, on job: {}. Ignoring RunTaskResult.",
                    result.taskId, result.jobId);
        }

        try {
            proc = dispatchService.getDispatchProc(result.procId);
        }
        catch (EmptyResultDataAccessException e) {
            logger.error("The proc {} no longer exists, was running task {}. Ignoring RunTaskResult.",
                    result.procId, result.taskId);
        }

        // TODO: might be possible to recover task rather than just returning.
        // in one of two ways.  If the task is waiting then we accept a good
        // RunTaskResult and mark it succeeded.  If its running, we can kill
        // the existing process and mark as succeeded.
        if (proc == null || task == null) {
            return;
        }

        PlowStats.rndTaskComplete.incrementAndGet();

        // May throw back to RND if the job's pipeline is full.
        // Will internally spawn a dispatch proc command.
        pipelineController.execute(
                new StopTaskCommand(result, task, proc, pipelineCommandService));
    }
}
