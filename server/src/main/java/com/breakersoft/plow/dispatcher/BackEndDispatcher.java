package com.breakersoft.plow.dispatcher;

import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.command.DispatchProcToJobCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.thrift.TaskState;

import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

public class BackEndDispatcher {

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    FrontEndDispatcher frontEndDispatcher;

    @Autowired
    JobService jobService;

    private ExecutorService threadPool;

    public BackEndDispatcher() throws Exception {

        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        factory.setCorePoolSize(8);
        factory.setMaxPoolSize(16);
        factory.setQueueCapacity(1000);
        threadPool = factory.getObject();
    }

    public void processRunTaskResult(RunTaskResult result) {

        Task task = jobService.getTask(result.taskId);
        Job job = jobService.getJob(result.jobId);
        DispatchProc proc = dispatchService.getDispatchProc(result.procId);

        if (result.exitStatus == 0) {
            jobService.setTaskState(task, TaskState.RUNNING, TaskState.SUCCEEDED);
        }
        else {
            jobService.setTaskState(task, TaskState.RUNNING, TaskState.DEAD);
        }
        // TODO depends


        if (!jobService.hasWaitingFrames(job)) {
            dispatchService.removeProc(proc);
        }

        if (!jobService.hasPendingFrames(job)) {
            dispatchService.removeProc(proc);
        }

        threadPool.execute(
                new DispatchProcToJobCommand(job, proc, frontEndDispatcher));
    }

}
