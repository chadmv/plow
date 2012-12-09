package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.command.BookProcCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchableTask;
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
    private ExecutorService dispatchThreads = Executors.newFixedThreadPool(8);


    public ProcDispatcher() {

    }

    public void book(DispatchProc proc) {
        dispatchThreads.execute(new BookProcCommand(proc, this));
    }

    public void dispatch(DispatchResult result, DispatchProc proc) {

        final List<DispatchableTask> tasks =
                dispatchService.getDispatchableTasks(proc.getJobId(), proc);

        for (DispatchableTask task: tasks) {
            dispatch(result, proc, task);
            if (!result.continueDispatching()){
                return;
            }
        }
    }

    public void dispatch(DispatchResult result, DispatchProc proc, DispatchableTask task) {

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
                result.dispatched(proc);
            }
            else {
                dispatchService.deallocateProc(proc, "Unable to book task.");
                dispatchService.unreserveTask(task);
            }
        }
        catch (Exception e) {
            logger.warn("Unexpected task dipatching error, " + e);
            dispatchService.deallocateProc(proc, e.getMessage());
            dispatchService.unreserveTask(task);
        }

        // Don't continue to dispatch.
        result.dispatch = false;
    }
}
