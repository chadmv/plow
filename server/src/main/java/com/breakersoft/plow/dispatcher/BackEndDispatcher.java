package com.breakersoft.plow.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.command.DispatchProcToJob;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.DependService;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.JobStateManager;
import com.breakersoft.plow.thrift.TaskState;

public class BackEndDispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(BackEndDispatcher.class);

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    DependService dependService;

    @Autowired
    FrontEndDispatcher frontEndDispatcher;

    @Autowired
    JobService jobService;

    @Autowired
    JobStateManager jobStateManager;

    private ExecutorService dispatchPool = Executors.newFixedThreadPool(8);

    public void taskComplete(RunTaskResult result) {

        Task task = jobService.getTask(result.taskId);
        DispatchJob job = frontEndDispatcher.getJob(task.getJobId());
        DispatchProc proc = dispatchService.getDispatchProc(result.procId);

        TaskState newState;
        if (result.exitStatus == 0) {
            newState =  TaskState.SUCCEEDED;
        }
        else {
            newState = TaskState.DEAD;
        }

        logger.info("{} proc reported in task completed: {}, exit status: {}",
                new Object[] { proc.getHostname(), proc.getTaskName(), result.exitStatus });
        logger.info("New state {}", newState.toString());

        if (dispatchService.stopTask(task, newState)) {
            dispatchService.unassignProc(proc);
            jobStateManager.satisfyDependsOn(task);

            final Layer layer = new LayerE(task);
            if (jobService.isLayerComplete(layer)) {
                jobStateManager.satisfyDependsOn(layer);
            }
        }
        else {
            // Task was already stopped somehow.
            // might be a retry or
            logger.warn("{} task was stopped by another thread.", proc.getTaskName());
            return;
        }

        if (!jobService.hasPendingFrames(job)) {
            dispatchService.unbookProc(proc,
                    "Job no longer has pending frames: " + job.getJobId());
            jobStateManager.shutdown(job);
        }
        else {
            dispatchPool.execute(new DispatchProcToJob(proc, job, frontEndDispatcher));
        }
    }
}
