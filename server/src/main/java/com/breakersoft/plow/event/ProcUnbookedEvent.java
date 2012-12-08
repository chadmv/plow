package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class ProcUnbookedEvent {

    private final DispatchProc proc;

    public ProcUnbookedEvent(DispatchProc proc) {
        this.proc = proc;
    }

    public DispatchProc getProc() {
        return proc;
    }
}
