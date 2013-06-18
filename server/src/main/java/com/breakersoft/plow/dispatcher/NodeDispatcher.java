package com.breakersoft.plow.dispatcher;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.rndaemon.RndClientPool;
import com.google.common.util.concurrent.RateLimiter;

@Component
public class NodeDispatcher extends AbstractDispatcher implements Dispatcher<DispatchNode>{

    @Autowired
    private RndClientPool rndClientPool;

    @Autowired
    @Qualifier("nodeDispatchExecutor")
    private ThreadPoolTaskExecutor nodeDispatchExecutor;

    private final RateLimiter rateLimiter = RateLimiter.create(12.0);

    public NodeDispatcher() { }

    public void asyncDispatch(final DispatchNode node) {
        nodeDispatchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final DispatchResult result = dispatch(node);
                if (result.procs > 0) {
                    PlowStats.nodeDispatchHit.incrementAndGet();
                }
                else {
                    PlowStats.nodeDispatchMiss.incrementAndGet();
                }
            }
        });
    }

    @Override
    public DispatchResult dispatch(DispatchNode node) {

        final DispatchResult result = new DispatchResult(node);

        if (!rateLimiter.tryAcquire()) {
            return result;
        }

        dispatch(result, node);
        return result;
    }

    @Override
    public void dispatch(final DispatchResult result, DispatchNode node) {

        final List<DispatchProject> projects =
                dispatchService.getSortedProjectList(node);

        if (projects.isEmpty()) {
            return;
        }

        for (DispatchProject project: projects) {
            dispatch(result, node, project);
            if (!result.continueDispatching()) {
                return;
            }
        }
    }

    @Override
    public void dispatch(final DispatchResult result, DispatchNode node, DispatchProject project) {

        // Return a list of jobs IDs that have pending frames for the job/node.
        final List<DispatchJob> jobs = dispatchService.getDispatchJobs(project, node);

        if (jobs.isEmpty()) {
            return;
        }

        for (DispatchJob job: jobs) {
             dispatch(result, node, job);
             if (!result.continueDispatching()) {
                 return;
             }
        }
    }

    @Override
    public void dispatch(final DispatchResult result, DispatchNode node, DispatchJob job) {

        final List<DispatchTask> tasks =
                dispatchService.getDispatchableTasks(job, node, 15);

        if (tasks.isEmpty()) {
            return;
        }

        for (DispatchTask task: tasks) {

            if (!result.canDispatch(task)) {
                continue;
            }

            if (!dispatchService.quotaCheck(node, task)) {
                result.continueDispatch = false;
                break;
            }

            dispatch(result, node, task);

            if (!result.continueDispatching()) {
                break;
            }
        }
    }

    @Override
    public void dispatch(final DispatchResult result, DispatchNode node, DispatchTask task) {

        if (!dispatchService.reserveTask(task)) {
            return;
        }

        DispatchProc proc = null;
        try {
            proc = dispatchService.allocateProc(node, task);
            PlowStats.procAllocCount.incrementAndGet();
        }
        catch (RuntimeException e) {
            dispatchFailed(result, proc, task, "Unable to allocate proc " + e);
            PlowStats.nodeDispatchFail.incrementAndGet();
            PlowStats.procAllocFailCount.incrementAndGet();
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
            PlowStats.nodeDispatchFail.incrementAndGet();
            PlowStats.taskStartedFailCount.incrementAndGet();
        }
    }
}
