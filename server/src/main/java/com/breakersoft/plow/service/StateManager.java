package com.breakersoft.plow.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.exceptions.PlowException;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.rndaemon.RndClient;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.util.PlowUtils;
import com.google.common.collect.Sets;

@Component
public class StateManager {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(StateManager.class);

    @Autowired
    JobService jobService;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    NodeService nodeService;

    @Autowired
    DependService dependService;

    @Autowired
    EventManager eventManager;

    @Autowired
    RndProcessManager processManager;

    public void killProc(Task task, boolean unbook) {
        final Proc proc = nodeService.getProc(task);
        if (unbook) {
            nodeService.setProcUnbooked(proc, true);
        }
        try {
            RndClient client = new RndClient(proc.getHostname());
            client.kill(proc, "Killed by user");
        } catch (RndClientExecuteException e) {
            logger.warn("Failed to stop running task: {}, {}", task.getTaskId(), e);

            // If this fails...then deallocate here.
            dispatchService.deallocateProc(proc, "Failed to communicate with RND");
        }
    }

    public void killTask(Task task) {
        logger.info("Thread: {} Killing Task: {}", Thread.currentThread().getName(), task.getTaskId());

        if (dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.MANUAL_KILL)) {
            killProc(task, true);
        }
    }

    public void eatTask(Task task, boolean checkFinished) {
        logger.info("Thread: {} Eating Task: {}", Thread.currentThread().getName(), task.getTaskId());

        if (dispatchService.stopTask(task, TaskState.EATEN, ExitStatus.FAIL, Signal.MANUAL_KILL)) {
            killProc(task, false);
        }
        else {
            jobService.setTaskState(task, TaskState.EATEN);
        }

        if (checkFinished) {
            Job job = jobService.getJob(task.getJobId());
            if (jobService.isJobFinished(job)) {
                  shutdownJob(job);
            }
        }
    }

    public void retryTask(final Task task) {
        logger.info("Thread: {} Retrying Task: {}", Thread.currentThread().getName(), task.getTaskId());

        // First try to stop the task, if that works kill the
        // running task.
        if (dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.MANUAL_RETRY)) {
            killProc(task, false);
        }
        else {
            // The trigger trig_before_update_set_depend handles not allowing
            // depend frames to go waiting.
            Job job = jobService.getJob(task.getJobId());
            // Only set the state to waiting if the job is not done.
            if (!jobService.isJobFinished(job)) {
                jobService.setTaskState(task, TaskState.WAITING);
            }
        }
    }

    @Async(value="stateChangeExecutor")
    public void retryTasks(TaskFilterT filter) {
        final List<Task> tasks = jobService.getTasks(filter);
        logger.info("Thread: {} Batch retrying {} Tasks", Thread.currentThread().getName(), tasks.size());
        for (final Task t: tasks) {
            retryTask(t);
        }
    }

    @Async(value="stateChangeExecutor")
    public void eatTasks(final TaskFilterT filter) {
        if (PlowUtils.isValid(filter.jobId)) {
            throw new PlowException("A jobId is not set on the task filter.");
        }

        final List<Task> tasks = jobService.getTasks(filter);
        logger.info("Thread: {} Batch eating {} tasks", Thread.currentThread().getName(), tasks.size());

        if (tasks.isEmpty()) {
            return;
        }

        Set<UUID> jobIds = Sets.newHashSet();
        for (final Task t: tasks) {
            eatTask(t, false);
            jobIds.add(t.getJobId());
        }

        for (UUID jobId: jobIds) {
            Job job = jobService.getJob(jobId);
            if (jobService.isJobFinished(job)) {
                shutdownJob(job);
            }
        }
    }

    @Async(value="stateChangeExecutor")
    public void killTasks(TaskFilterT filter) {
        final List<Task> tasks = jobService.getTasks(filter);
        logger.info("Thread: {} Batch killing {} tasks", Thread.currentThread().getName(), tasks.size());
        for (final Task t: tasks) {
            killTask(t);
        }
    }

    @Async(value="stateChangeExecutor")
    public void killJob(Job job, String reason) {
        final boolean killResult = shutdownJob(job);
        if (killResult) {
            PlowStats.jobKillCount.incrementAndGet();
            processManager.killProcs(job, reason);
        }
    }

    public boolean shutdownJob(Job job) {
        if (jobService.shutdown(job)) {
            logger.info("Shutting down job {}", job);
            PlowStats.jobFinishCount.incrementAndGet();
            satisfyDependsOn(job);
            eventManager.post(new JobFinishedEvent(job));
            return true;
        }
        return false;
    }

    @Async(value="stateChangeExecutor")
    public void createDepend(DependSpecT depend) {
        dependService.createDepend(depend);
    }

    @Async(value="stateChangeExecutor")
    public void satisfyDepend(Depend depend) {
        logger.trace("Satisfying dependency {}", depend);
        dependService.satisfyDepend(depend);
    }

    @Async(value="stateChangeExecutor")
    public void unsatisfyDepend(Depend depend) {
        logger.info("Reactivating dependency {}", depend);
        dependService.unsatisfyDepend(depend);
    }

    public void satisfyDependsOn(Job job) {
        final List<Depend> depends = dependService.getOnJobDepends(job);
        logger.trace("{} has {} dependencies.", job, depends.size());
        for (Depend depend: depends) {
            dependService.satisfyDepend(depend);
        }
    }

    public void satisfyDependsOn(Task task) {
        final List<Depend> depends = dependService.getOnTaskDepends(task);
        logger.trace("{} has {} dependencies.", task, depends.size());
        for (Depend depend: depends) {
            dependService.satisfyDepend(depend);
        }
    }

    public void satisfyDependsOn(Layer layer) {
        final List<Depend> depends = dependService.getOnLayerDepends(layer);
        logger.trace("{} has {} dependencies.", layer, depends.size());
        for (Depend depend: depends) {
            dependService.satisfyDepend(depend);
        }
    }
}
