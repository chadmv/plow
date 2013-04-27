package com.breakersoft.plow.crond;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;

/**
 *
 * Cleans up orphaned procs.
 *
 * @author chambers
 *
 */
public class OrphanProcChecker {

    private static final Logger logger = LoggerFactory.getLogger(OrphanProcChecker.class);

    @Autowired
    DispatchService dispatchService;

    public void execute() {
        List<DispatchProc> procs = dispatchService.getOrphanProcs();
        logger.info("Orphan proc checker found {} orphan procs.", procs.size());

        for (DispatchProc proc: procs) {


            logger.warn("Deallocating orphan {}", proc);
            dispatchService.deallocateProc(proc, "orphaned");



        }
    }

}
