package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class ProcDeallocatedEvent {

    public DispatchProc proc;

    public ProcDeallocatedEvent(DispatchProc proc) {
        this.proc = proc;
    }
}
