package com.breakersoft.plow.dispatcher;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchResult;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.thrift.TaskState;

public class AbstractDispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(AbstractDispatcher.class);

    @Autowired
    protected DispatchService dispatchService;

    protected void dispatchFailed(DispatchResult result, DispatchProc proc, DispatchTask task, String message) {
        logger.error("Unable to dispatch {}/{}, {}", new Object[] {proc, task, message});

        if (task != null) {
            if (task.started) {
                dispatchService.stopTask(task, TaskState.WAITING, ExitStatus.FAIL, Signal.ABORTED_TASK);
            }
            else {
                dispatchService.unreserveTask(task);
            }
        }
        dispatchService.markAsDeallocated(proc);
        result.continueDispatch = false;
    }
}
