package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.dispatcher.FrontEndDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class DispatchProcToJobCommand implements Runnable {

    private final Job job;
    private final DispatchProc proc;
    private final FrontEndDispatcher dispatcher;

    public DispatchProcToJobCommand(Job job, DispatchProc proc, FrontEndDispatcher dispatcher) {
        this.proc = proc;
        this.job = job;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        dispatcher.dispatch(job, proc);
    }
}
