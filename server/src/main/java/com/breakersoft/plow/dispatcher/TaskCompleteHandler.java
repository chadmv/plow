package com.breakersoft.plow.dispatcher;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.DependService;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.TaskState;

@Component
public class TaskCompleteHandler {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(TaskCompleteHandler.class);

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

    public void taskComplete(RunTaskResult result) {

        Task task = jobService.getTask(result.taskId);
        Job job = jobService.getJob(result.jobId);
        DispatchProc proc = dispatchService.getDispatchProc(result.procId);

        TaskState newState;
        if (result.exitStatus == 0) {
            newState = TaskState.SUCCEEDED;
        }
        else {
            newState = TaskState.DEAD;
        }

        logger.info("New state {}", newState.toString());

        if (dispatchService.stopTask(task, newState)) {
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
