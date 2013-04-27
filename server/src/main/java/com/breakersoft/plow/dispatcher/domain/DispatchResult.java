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
    public boolean dispatch = true;
    public boolean isTest = false;
    public int maxProcs = 8;
    public int maxCores = DispatchConfig.MAX_CORES_PER_JOB;

    public List<DispatchProc> procs = Lists.newArrayList();

    public void setMaxCores(int max) {
        this.maxCores = Math.min(max, DispatchConfig.MAX_CORES_PER_JOB);
    }

    public DispatchResult(DispatchResource resource) {
        this.resource = resource;
    }

    /**
     * Check if the given resource can still be dispatched.
     *
     * @return
     */
    public boolean continueDispatching() {

        if (procs.size() >= maxProcs) {
            logger.info("Stopped dispatching by procs/maxprocs {} >= {}", procs.size(), maxProcs);
            return false;
        }

        if (cores >= maxCores) {
            logger.info("Stopped dispatching by cores/maxcores {} >= {}", cores, maxCores);
        }

        if (procs.size() >= DispatchConfig.MAX_PROCS_PER_JOB) {
            logger.info("Stopped dispatching by procs/max_per_job {} >= {}",
                    procs.size(), DispatchConfig.MAX_PROCS_PER_JOB);
            return false;
        }

        if (resource.getIdleCores() < 1) {
            logger.info("Stopped dispatching resource cores {} < 1", resource.getIdleCores());
            return false;
        }

        if (resource.getIdleRam() <=  0) {
            logger.info("Stopped dispatching resource ram {} <=0", resource.getIdleRam());
            return false;
        }

        return dispatch;
    }

    public boolean canDispatch(DispatchTask task) {

        if (resource.getIdleCores() < task.minCores) {
            logger.info("Stopped dispatching by cores {} < {}", resource.getIdleCores(), task.minCores);
            return false;
        }

        if (resource.getIdleRam() < task.minRam) {
            logger.info("Stopped dispatching by ram {} < {}", resource.getIdleRam(), task.minRam);
            return false;
        }

        return true;
    }

    public void dispatched(DispatchProc proc, DispatchTask task) {
        // Here is why all ram gets eaten up.
        cores+=task.minCores;
        ram+=task.minRam;

        procs.add(proc);
        resource.allocate(task.minCores, task.minRam);

        logger.info("Dispatched {}, cores left: {} - ram left: {}",
                new Object[] { proc.getHostname(), resource.getIdleCores(),
                resource.getIdleRam()});
    }
}
