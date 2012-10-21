package com.breakersoft.plow.event;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;

public class JobBookedEvent {

    private final DispatchTask task;
    private final DispatchProc proc;

    public JobBookedEvent(DispatchProc proc, DispatchTask task) {
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
