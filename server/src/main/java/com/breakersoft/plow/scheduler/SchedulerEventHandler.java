package com.breakersoft.plow.scheduler;

import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.NodeDispatcher;
import com.breakersoft.plow.dispatcher.ProcDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.DependService;
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
public class SchedulerEventHandler {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(SchedulerEventHandler.class);

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    NodeService nodeService;

    @Autowired
    NodeDispatcher nodeDispatcher;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    DependService dependService;

    @Autowired
    JobService jobService;

    @Autowired
    StateManager jobStateManager;

    @Autowired
    ProcDispatcher procDispatcher;

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


        if (node.isDispatchable()) {
            nodeDispatcher.book(node);
        }
    }

    public void handleRunTaskResult(RunTaskResult result) {

        Task task = null;
        Job job = null;
        DispatchProc proc = null;
        try {
            task = jobService.getTask(UUID.fromString(result.taskId));
            job = jobService.getJob(UUID.fromString(result.jobId));
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

        logger.info("New state {}", newState.toString());

        if (dispatchService.stopTask(task, newState, result.exitStatus, result.exitSignal)) {
            dispatchService.unassignProc(proc);

            if (newState.equals(TaskState.SUCCEEDED)) {
                jobStateManager.satisfyDependsOn(task);
                final Layer layer = new LayerE(task);
                if (jobService.isLayerComplete(layer)) {
                    jobStateManager.satisfyDependsOn(layer);
                }
            }
        }

        if (proc.isUnbooked()) {
            dispatchService.deallocateProc(proc, "Task was stopped");
        }

        if (jobService.isJobPaused(job)) {
            dispatchService.deallocateProc(proc,
                    "Job is paused: " + job.getJobId());
            return;
        }

        if (jobService.isFinished(job)) {
            dispatchService.deallocateProc(proc,
                    "Job is finished " + job.getJobId());
            jobStateManager.shutdownJob(job);
            return;
        }

        procDispatcher.book(proc);
    }
}
