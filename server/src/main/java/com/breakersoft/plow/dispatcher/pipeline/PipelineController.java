package com.breakersoft.plow.dispatcher.pipeline;

import java.util.Collection;

import com.breakersoft.plow.JobId;

public interface PipelineController {

     public void execute(PipelineCommand command);
     public void execute(JobId id, Collection<PipelineCommand> commands);
}
