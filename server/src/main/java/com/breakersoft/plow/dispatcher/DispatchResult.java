package com.breakersoft.plow.dispatcher;

import java.util.List;

import org.slf4j.Logger;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;
import com.google.common.collect.Lists;

public class DispatchResult {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchResult.class);

    public final DispatchResource resource;
    public int cores = 0;
    public int ram = 0;
    public boolean dispatch = true;
    public boolean isTest = false;

    public List<DispatchProc> procs = Lists.newArrayList();

    public DispatchResult(DispatchResource resource) {
        this.resource = resource;
    }

    public boolean continueDispatching() {

        if (resource.getIdleCores() - cores < 1) {
            return false;
        }

        if (resource.getIdleRam() - ram <= Defaults.MEMORY_RESERVE_MB) {
            return false;
        }

        return dispatch;
    }

    public void dispatched(DispatchProc proc) {
        procs.add(proc);
        cores+=proc.getIdleCores();
        ram+=proc.getIdleRam();

        logger.info("Dispatched {}, cores left: {} - ram left: {}",
                new Object[] { proc.getHostname(), resource.getIdleCores() - cores,
                resource.getIdleRam() - ram});

    }
}
