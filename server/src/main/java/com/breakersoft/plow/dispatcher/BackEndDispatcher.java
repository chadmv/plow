package com.breakersoft.plow.dispatcher;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.thrift.TaskState;

import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

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

    private ExecutorService threadPool;

    public BackEndDispatcher() throws Exception {

        ThreadPoolExecutorFactoryBean factory = new ThreadPoolExecutorFactoryBean();
        factory.setCorePoolSize(8);
        factory.setMaxPoolSize(16);
        factory.setQueueCapacity(1000);
        threadPool = factory.getObject();
    }

    public void taskComplete(RunTaskResult result) {

        Task task = jobService.getTask(result.taskId);
        DispatchProc proc = dispatchService.getDispatchProc(result.procId);

        TaskState newState;

        if (result.exitStatus == 0) {
            newState =  TaskState.SUCCEEDED;
        }
        else {
            newState = TaskState.DEAD;
        }

        logger.info("{} proc reported in task completed, exit status:",
                proc.getNodeName(), result.exitStatus);

        logger.info("New state {}", newState.toString());

        if (!jobService.stopTask(task, newState)) {
            // Task was already stopped somehow.
            // might be a retry or
            logger.warn("{} task was stopped by another thread.", task.getTaskId());
            return;
        }

        logger.info("Clearing proc");
        dispatchService.removeProc(proc);
    }

}
