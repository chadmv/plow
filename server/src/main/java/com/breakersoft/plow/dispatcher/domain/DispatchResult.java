package com.breakersoft.plow.dispatcher.domain;

import java.util.List;

import org.slf4j.Logger;

import com.breakersoft.plow.dispatcher.DispatchConfig;
import com.google.common.collect.Lists;

public class DispatchResult {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchResult.class);

    public final DispatchResource resource;
    public DispatchProfile profile = new DispatchProfile();

    public int cores = 0;
    public int ram = 0;
    public int procs = 0;
    public boolean continueDispatch = true;
    public boolean isTest = false;
    public long startTime = System.currentTimeMillis();
    public int reservationFailures = 0;

    public final List<DispatchPair> pairs =
            Lists.newArrayListWithExpectedSize(DispatchConfig.MAX_PROCS_PER_JOB);

    public DispatchResult(DispatchResource resource) {
        this.resource = resource;
    }

    /**
     * Check if the given resource can still be dispatched.
     *
     * @return
     */
    public boolean continueDispatching() {

        if (reservationFailures >= 5) {
            logger.trace("Stopped dispatching due to contention.");
            return false;
        }

        if (pairs.size() >= DispatchConfig.MAX_PROCS_PER_JOB) {
            logger.trace("Stopped dispatching by procs/maxprocs {} >= {}", pairs.size(),  DispatchConfig.MAX_PROCS_PER_JOB);
            return false;
        }

        if (cores >= DispatchConfig.MAX_CORES_PER_JOB) {
            logger.trace("Stopped dispatching by cores/maxcores {} >= {}", cores, DispatchConfig.MAX_CORES_PER_JOB);
            return false;
        }

        if (resource.getIdleCores() < 1) {
            logger.trace("Stopped dispatching resource cores {} < 1", resource.getIdleCores());
            return false;
        }

        if (resource.getIdleRam() <=  0) {
            logger.trace("Stopped dispatching resource ram {} <=0", resource.getIdleRam());
            return false;
        }

        return continueDispatch;
    }

    public boolean canDispatch(DispatchTask task) {

        if (resource.getIdleCores() < task.minCores) {
            logger.trace("Stopped dispatching by cores {} < {}", resource.getIdleCores(), task.minCores);
            return false;
        }

        if (resource.getIdleRam() < task.minRam) {
            logger.info("Stopped dispatching by ram {} < {}", resource.getIdleRam(), task.minRam);
            return false;
        }

        return true;
    }

    public void addDispatchPair(DispatchProc proc, DispatchTask task) {
        cores+=task.minCores;
        ram+=task.minRam;
        procs++;

        pairs.add(new DispatchPair(proc, task));
        resource.allocate(task.minCores, task.minRam);
    }
}
