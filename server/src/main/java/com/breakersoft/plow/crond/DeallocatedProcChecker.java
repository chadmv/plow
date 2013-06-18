package com.breakersoft.plow.crond;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;

@Component
public class DeallocatedProcChecker extends AbstractCrondTask {

    @Autowired
    DispatchService dispatchService;

    public DeallocatedProcChecker() {
        super(CrondTask.DEALLOC_PROC_CHECK);
    }

    public void run() {
        List<DispatchProc> procs = dispatchService.getDeallocatedProcs();
        for(DispatchProc proc: procs) {
            logger.info("Deallocating proc: {}", proc);
            dispatchService.deallocateProc(proc, "Deallocated by the dispatcher, no longer needed.");
        }
    }
}
