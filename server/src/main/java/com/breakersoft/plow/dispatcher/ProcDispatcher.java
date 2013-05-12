package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.dispatcher.command.BookProcCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.rnd.thrift.RunTaskCommand;
import com.breakersoft.plow.rndaemon.RndClient;
import com.breakersoft.plow.thrift.TaskState;

/**
 *
 * Logic for redispatching an existing proc.  Existing procs can only
 * go to the same job.
 *
 * @author chambers
 *
 */
@Component
public class ProcDispatcher implements Dispatcher<DispatchProc> {

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
                /*
                 * We had reserved the task but were somehow unable to start it,
                 */
                dispatchFailed(result, proc, null, "Critical, unable to start reserved task.");
            }
        }
        catch (Exception e) {
            logger.warn("Unexpected task dipatching error, " + e, e);
            dispatchFailed(result, proc, task, e.getMessage());
        }
    }

    @Override
    public void dispatch(DispatchResult result, DispatchProc resource,
            DispatchProject project) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispatch(DispatchResult result, DispatchProc resource,
            DispatchJob job) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispatchFailed(DispatchResult result, DispatchProc resource,
            DispatchTask task, String message) {

        logger.info("Unable to dispatch {}/{}, {}", new Object[] {resource, task, message});

        dispatchService.deallocateProc(resource, message);
        if (task != null) {
            dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.ABORTED_TASK);
        }
        result.dispatch = false;
    }
}
