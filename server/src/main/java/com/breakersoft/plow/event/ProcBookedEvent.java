package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;

public class ProcBookedEvent {

    private final DispatchTask task;
    private final DispatchProc proc;

    public ProcBookedEvent(DispatchProc proc, DispatchTask task) {
        this.task = task;
        this.proc = proc;
    }

    public DispatchTask getTask() {
        return task;
    }

    public DispatchProc getProc() {
        return proc;
    }
}
