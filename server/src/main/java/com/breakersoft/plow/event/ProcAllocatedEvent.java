package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class ProcAllocatedEvent {

    public DispatchProc proc;

    public ProcAllocatedEvent(DispatchProc proc) {
        this.proc = proc;
    }

}
