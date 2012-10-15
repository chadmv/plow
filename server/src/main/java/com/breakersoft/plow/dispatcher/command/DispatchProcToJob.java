package com.breakersoft.plow.dispatcher.command;

import com.breakersoft.plow.dispatcher.FrontEndDispatcher;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class DispatchProcToJob implements DispatchCommand {

    private DispatchJob dispatchJob;
    private DispatchProc dispatchProc;
    private FrontEndDispatcher dispatcher;

    public DispatchProcToJob(DispatchProc dispatchProc,
            DispatchJob dispatchJob, FrontEndDispatcher dispatcher) {
        this.dispatchProc = dispatchProc;
        this.dispatchJob = dispatchJob;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        dispatcher.dispatch(dispatchProc, dispatchJob);
    }

}
