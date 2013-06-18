package com.breakersoft.plow.dispatcher.pipeline;


import org.slf4j.Logger;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.thrift.TaskState;

public class AbortedTaskCommand extends PipelineCommand {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(AbortedTaskCommand.class);

    private final Task task;
    private final DispatchProc proc;

    private final DispatchService dispatchService;

    public AbortedTaskCommand(DispatchProc proc, Task task, DispatchService dispatchService) {
        super(task.getJobId());
        this.proc = proc;
        this.task = task;
        this.dispatchService = dispatchService;
    }

    @Override
    public void process() {
        try {
            dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.ABORTED_TASK);
        } catch (RuntimeException e) {
            logger.warn("Failed to stop and abort task: {}, unexpected {}", task, e);
        }

        // Unassign the proc from the task.
        dispatchService.unassignProc(proc);
        // Mark it for deallocation.
        dispatchService.markAsDeallocated(proc);
    }
}
