package com.breakersoft.plow.dispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.command.DispatchProcToJob;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.thrift.TaskState;

public class BackEndDispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(BackEndDispatcher.class);

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    FrontEndDispatcher frontEndDispatcher;

    @Autowired
    JobService jobService;

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
                new Object[] { proc.getNodeName(), proc.getTaskName(), result.exitStatus });
        logger.info("New state {}", newState.toString());

        if (!jobService.stopTask(task, newState)) {
            // Task was already stopped somehow.
            // might be a retry or
            logger.warn("{} task was stopped by another thread.", proc.getTaskName());
            return;
        }
        else {
            dispatchService.unassignProc(proc);
        }

        if (!jobService.hasPendingFrames(job)) {
            jobService.shutdown(job);
            dispatchService.unbookProc(proc);
        }
        dispatchPool.execute(new DispatchProcToJob(proc, job, frontEndDispatcher));
    }

}
