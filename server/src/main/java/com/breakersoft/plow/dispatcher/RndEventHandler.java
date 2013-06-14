package com.breakersoft.plow.dispatcher;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.TaskState;

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
    SchedulerService schedulerService;

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
    StateManager stateManager;

    @Autowired
    ProcDispatcher procDispatcher;

    /**
     * This is a oneway method, the RNDaemon is not listening for a response.
     *
     * @param ping
     */
    public void handleNodePing(Ping ping) {

        logger.info("{} node reporting in.", ping.getHostname());

        DispatchNode node;
        try {
            node = dispatchService.getDispatchNode(ping.hostname);
            nodeService.updateNode(node, ping);
        } catch (EmptyResultDataAccessException e) {
            Node newNode = nodeService.createNode(ping);
            node = dispatchService.getDispatchNode(newNode.getName());
        }

        if (!ping.tasks.isEmpty()) {
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

        TaskState newState;
        if (result.exitStatus == 0) {
            newState = TaskState.SUCCEEDED;
        }
        else {

            if (dispatchService.isAtMaxRetries(task)) {
                newState = TaskState.DEAD;
            }
            else {
                newState = TaskState.WAITING;
            }
        }

        if (dispatchService.stopTask(
                task, newState, result.exitStatus, result.exitSignal)) {

            logger.info("{} new state {}", task, newState.toString());
            dispatchService.unassignProc(proc);

            if (newState.equals(TaskState.SUCCEEDED)) {
                stateManager.satisfyDependsOn(task);
                final Layer layer = new LayerE(task);
                if (jobService.isLayerComplete(layer)) {
                    stateManager.satisfyDependsOn(layer);
                }
                // Async
                stateManager.asyncTaskSucceeded(task);
            }
        }
        else {
            logger.error("{} NO NEW STATE, was not able to stop task", task);
            // Not sure what happened to the proc here.
            dispatchService.deallocateProc(proc, "Returned result for an already compelted task.");
            return;
        }

        try {
            procDispatcher.asyncDispatch(proc);
        } catch (Exception e) {
             dispatchService.deallocateProc(proc, "Unable to execute async proc dispatch:" + e);
        }
    }
}
