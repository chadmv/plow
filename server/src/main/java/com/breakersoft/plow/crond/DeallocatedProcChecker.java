package com.breakersoft.plow.crond;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.monitor.PlowStats;

public class DeallocatedProcChecker extends AbstractCrondTask {

    @Autowired
    DispatchService dispatchService;

    public DeallocatedProcChecker() {
        super(CrondTask.DEALLOC_PROC_CHECK);
    }

    protected void run() {
        List<DispatchProc> procs = dispatchService.getDeallocatedProcs();
        for(DispatchProc proc: procs) {
            try {
                logger.info("Deallocating proc: {}", proc);
                dispatchService.deallocateProc(proc, "Deallocated by the dispatcher, no longer needed.");
                PlowStats.procUnallocCount.incrementAndGet();
            } catch (RuntimeException e) {
                logger.warn("Failed to deallocate proc: {}, unexepected: {}", proc, e);
                PlowStats.procUnallocFailCount.incrementAndGet();
            }
        }
    }
}
