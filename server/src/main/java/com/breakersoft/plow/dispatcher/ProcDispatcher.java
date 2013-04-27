package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.command.BookProcCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.rndaemon.RndClient;

/**
 *
 * Logic for redispatching an existing proc.  Existing procs can only
 * go to the same job.
 *
 * @author chambers
 *
 */
@Component
public class ProcDispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(ProcDispatcher.class);

    @Autowired
    private DispatchService dispatchService;

    // Threads for dispatching.
    private ExecutorService dispatchThreads = Executors.newFixedThreadPool(16);


    public ProcDispatcher() {

    }

    public void book(DispatchProc proc) {
        dispatchThreads.execute(new BookProcCommand(proc, this));
    }

    public void dispatch(DispatchResult result, DispatchProc proc) {

        final List<DispatchTask> tasks =
                dispatchService.getDispatchableTasks(proc, proc);

        if (tasks.isEmpty()) {
            dispatchService.deallocateProc(proc, "No pending tasks for job: " + proc.getJobId());
            return;
        }

        for (DispatchTask task: tasks) {
            dispatch(result, proc, task);
            // Only continue if the task failed due to a
            // reservation problem.
            if (!result.continueDispatching()) {
                break;
            }
        }
    }

    public void dispatch(DispatchResult result, DispatchProc proc, DispatchTask task) {

        if (!dispatchService.reserveTask(task)) {
            return;
        }

        try {
            dispatchService.assignProc(proc, task);
            if (dispatchService.startTask(proc.getHostname(), task)) {
                RunTaskCommand command =
                        dispatchService.getRuntaskCommand(task);
                RndClient client = new RndClient(proc.getHostname());
                client.runProcess(command);
                result.dispatched(proc, task);
                // Don't continue to dispatch.
                result.dispatch = false;
            }
            else {
                cleanup(result, proc, task, "Unable to book task, was already booked?");
            }
        }
        catch (Exception e) {
            logger.warn("Unexpected task dipatching error, " + e);
            cleanup(result, proc, task, e.getMessage());
        }
    }

    private void cleanup(DispatchResult result, DispatchProc proc, DispatchTask task, String message) {
        dispatchService.deallocateProc(proc, message);
        dispatchService.unreserveTask(task);
        result.dispatch = false;
    }
}
