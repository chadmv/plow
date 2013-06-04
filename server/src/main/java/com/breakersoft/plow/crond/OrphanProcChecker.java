package com.breakersoft.plow.crond;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.thrift.TaskState;

/**
 *
 * Cleans up orphaned procs.
 *
 * @author chambers
 *
 */
public class OrphanProcChecker extends AbstractCrondTask {

    private static final Logger logger = LoggerFactory.getLogger(OrphanProcChecker.class);

    @Autowired
    DispatchService dispatchService;

    @Autowired
    JobService jobService;

    public OrphanProcChecker() {
        super(CrondTask.ORPHAN_PROC_CHECK);
    }

    protected void run() {

        final List<DispatchProc> procs = dispatchService.getOrphanProcs();
        logger.info("Orphan proc checker found {} orphan procs.", procs.size());

        for (DispatchProc proc: procs) {

            try {

                final UUID taskId = proc.getTaskId();

                if (proc.getTaskId() != null) {
                    final Task task = jobService.getTask(taskId);

                    logger.warn("Found orphaned {}", task);
                    dispatchService.stopTask(task, TaskState.WAITING,
                            ExitStatus.FAIL, Signal.ORPANED_TASK);
                }

                logger.warn("Deallocating orphan {}", proc);
                dispatchService.deallocateProc(proc, "orphaned");

            } catch (Exception e) {
                logger.warn("Failed to handled orphaned proc, " + e.getMessage(), e);
            }
        }
    }
}
