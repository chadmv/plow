package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class JobUnbookedEvent {

    private final DispatchProc proc;

    public JobUnbookedEvent(DispatchProc proc) {
        this.proc = proc;
    }

    public DispatchProc getProc() {
        return proc;
    }
}
