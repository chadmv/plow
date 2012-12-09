package com.breakersoft.plow.dispatcher.domain;

import java.util.List;

import org.slf4j.Logger;

import com.breakersoft.plow.dispatcher.DispatchConfig;
import com.google.common.collect.Lists;

public class DispatchResult {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchResult.class);

    public final DispatchResource resource;
    public DispatchProfile profile;

    public int cores = 0;
    public int ram = 0;
    public boolean dispatch = true;
    public boolean isTest = false;
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

        if (cores >= maxCores) {
            return false;
        }

        if (procs.size() >= DispatchConfig.MAX_PROCS_PER_JOB) {
            return false;
        }

        if (resource.getIdleCores() < 1) {
            return false;
        }

        if (resource.getIdleRam() <=  0) {
            return false;
        }

        return dispatch;
    }

    public boolean canDispatch(DispatchableTask task) {

        if (resource.getIdleCores() < task.minCores) {
            return false;
        }

        if (resource.getIdleRam() < task.minRam) {
            return false;
        }

        return true;
    }

    public void dispatched(DispatchProc proc) {
        cores+=proc.getIdleCores();
        ram+=proc.getIdleRam();

        procs.add(proc);
        resource.allocate(proc.getIdleCores(), proc.getIdleRam());

        logger.info("Dispatched {}, cores left: {} - ram left: {}",
                new Object[] { proc.getHostname(), resource.getIdleCores() - cores,
                resource.getIdleRam() - ram});
    }
}
