package com.breakersoft.plow.dispatcher.pipeline;

import com.breakersoft.plow.dispatcher.domain.DispatchProc;

public class DispatchProcCommand extends PipelineCommand {

    private final DispatchProc proc;
    private final PipelineCommandService service;

    public DispatchProcCommand(DispatchProc proc, PipelineCommandService service) {
        super(proc.getJobId());
        this.proc = proc;
        this.service = service;
    }

    @Override
    public void process() {
        service.dispatchProc(proc);
    }
}
