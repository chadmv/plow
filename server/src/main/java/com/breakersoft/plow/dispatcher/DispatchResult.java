package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResource;

public class DispatchResult {

    public final DispatchResource resource;
    public int cores = 0;
    public int ram = 0;
    public int procs = 0;
    public boolean dispatch = true;
    public boolean isTest = false;

    public DispatchResult(DispatchResource resource) {
        this.resource = resource;
    }

    public boolean continueDispatching() {

        if (resource.getIdleCores() - cores > 0) {
            return true;
        }

        if (resource.getIdleRam() - ram > Defaults.MEMORY_RESERVE_MB) {
            return true;
        }

        return dispatch;
    }

    public void dispatched(DispatchProc proc) {
        procs++;
        cores+=proc.getIdleCores();
        ram+=proc.getIdleRam();
    }
}
