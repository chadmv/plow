package com.breakersoft.plow.dispatcher.pipeline;

import java.util.UUID;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.rnd.thrift.RunTaskResult;

public class StopTaskCommand extends PipelineCommand {

    private RunTaskResult runTaskResult;
    private PipelineCommandService service;

    public StopTaskCommand(RunTaskResult result, Task task, DispatchProc proc, PipelineCommandService service) {
        super(UUID.fromString(result.jobId));
        this.runTaskResult = result;
        this.service = service;
    }

    @Override
    public void process() {

    }
}
