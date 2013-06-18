package com.breakersoft.plow.dispatcher.pipeline;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.ProcDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.TaskState;

@Component
public class PipelineCommandService {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(PipelineCommandService.class);

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private ProcDispatcher procDispatcher;

    @Autowired
    private JobService jobService;

    @Autowired
    private StateManager stateManager;

    void stopTask(RunTaskResult result, Task task, DispatchProc proc) {

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

        boolean deallocateProc = false;
        if (dispatchService.stopTask(
                task, newState, result.exitStatus, result.exitSignal)) {
            dispatchService.unassignProc(proc);
            PlowStats.taskStoppedCount.incrementAndGet();

            if (newState.equals(TaskState.SUCCEEDED)) {

                stateManager.satisfyDependsOn(task);
                final Layer layer = new LayerE(task);
                if (jobService.isLayerComplete(layer)) {
                    stateManager.satisfyDependsOn(layer);
                }

                try {
                    if (jobService.isFinished(task)) {
                        deallocateProc = true;
                        final Job job = jobService.getJob(task.getJobId());
                        stateManager.shutdownJob(job);
                    }
                } catch (RuntimeException e) {
                    deallocateProc = true;
                    logger.warn("Failed to shutdown job after task complete {}", task);
                }

                //TODO: do I even need this anymore once durable pipeline works?
                dispatchService.dependQueueProcessed(task);
            }
        }
        else {
            deallocateProc = true;
            PlowStats.taskStoppedFailCount.incrementAndGet();
            logger.error("Unable to stop stop task {}", task);
        }

        if (!jobService.isDispatchable(proc)) {
            deallocateProc = true;
        }

        if (deallocateProc) {
            dispatchService.markAsDeallocated(proc);
        }
        else {
            procDispatcher.dispatch(proc);
        }
    }

    void dispatchProc(DispatchProc proc) {
        procDispatcher.dispatch(proc);
    }
}
