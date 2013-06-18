package com.breakersoft.plow.dispatcher;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.rndaemon.RndClientPool;

/**
 *
 * Logic for redispatching an existing proc.  Existing procs can only
 * go to the same job.
 *
 * @author chambers
 *
 */
@Component
public class ProcDispatcher extends AbstractDispatcher implements Dispatcher<DispatchProc> {

    @Autowired
    private RndClientPool rndClientPool;

    @Override
    public DispatchResult dispatch(DispatchProc proc) {
        final DispatchResult result = new DispatchResult(proc);
        dispatch(result, proc);
        if (result.procs == 0) {
            dispatchService.markAsDeallocated(proc);
            PlowStats.procDispatchMiss.incrementAndGet();
        }
        else {
            PlowStats.procDispatchHit.incrementAndGet();
        }
        return result;
    }

    public void dispatch(final DispatchResult result, DispatchProc proc) {

        final List<DispatchTask> tasks =
                dispatchService.getDispatchableTasks(proc, proc, 25);

        for (DispatchTask task: tasks) {

            if (!result.continueDispatching()) {
                break;
            }

            dispatch(result, proc, task);
        }
    }

    @Override
    public void dispatch(DispatchResult result, DispatchProc proc, DispatchTask task) {

        if (!dispatchService.reserveTask(task)) {
            return;
        }

        try {
            dispatchService.assignProc(proc, task);
        } catch (RuntimeException e) {
            dispatchFailed(result, proc, task, "Unable to assign proc to a resereved task.");
            PlowStats.procDispatchFail.incrementAndGet();
            return;
        }

        if (dispatchService.startTask(task, proc)) {
            result.addDispatchPair(proc, task);
            if (!result.isTest) {
                rndClientPool.executeProcess(proc, task);
            }
            PlowStats.taskStartedCount.incrementAndGet();
        }
        else {
            dispatchFailed(result, proc, task, "Critical, was able to reserve task but not start it.");
            PlowStats.procDispatchFail.incrementAndGet();
            PlowStats.taskStartedFailCount.incrementAndGet();
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


}
