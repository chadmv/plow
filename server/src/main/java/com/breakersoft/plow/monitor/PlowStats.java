package com.breakersoft.plow.monitor;

import java.util.concurrent.atomic.AtomicLong;

public class PlowStats {

    /**
     * NODES
     */

    /*
     * The NodeDispatcher actually dispatched some cores.
     */
    public static final AtomicLong nodeDispatchHit = new AtomicLong(0L);

    /*
     * The NodeDispatcher didn't find any work to do.
     */
    public static final AtomicLong nodeDispatchMiss = new AtomicLong(0L);

    /*
     * The NodeDispatcher actually failed and aborted.
     */
    public static final AtomicLong nodeDispatchFail = new AtomicLong(0L);

    /**
     *  RND
     */

    /*
     * Counts the nodes pinging in.
     */
    public static final AtomicLong rndPingCount = new AtomicLong(0L);


    public static final AtomicLong rndTaskComplete = new AtomicLong(0L);

    /**
     * PROCS
     */

    /*
     * The ProcDispatcher actually dispatched some cores.
     */
    public static final AtomicLong procDispatchHit = new AtomicLong(0L);

    /*
     * The NodeDispatcher didn't find any work to do.
     */
    public static final AtomicLong procDispatchMiss = new AtomicLong(0L);

    /*
     * The NodeDispatcher actually failed and aborted.
     */
    public static final AtomicLong procDispatchFail = new AtomicLong(0L);

    /*
     * Procs allocated.
     */
    public static final AtomicLong procAllocCount = new AtomicLong(0L);

    /*
     * Procs unallocated
     */
    public static final AtomicLong procUnallocCount = new AtomicLong(0L);

    /*
     * Proc allocations failed
     */
    public static final AtomicLong procAllocFailCount = new AtomicLong(0L);

    /*
     * Procs unallocated failed
     */
    public static final AtomicLong procUnallocFailCount = new AtomicLong(0L);

    /**
     * Tasks
     */

    /*
     * Count of the number of tasks started
     */
    public static final AtomicLong taskStartedCount =  new AtomicLong(0L);

    /*
     * Count of the number of tasks start failures
     */
    public static final AtomicLong taskStartedFailCount =  new AtomicLong(0L);

    /*
     * Count of the number of tasks stopped
     */
    public static final AtomicLong taskStoppedCount =  new AtomicLong(0L);

    /*
     * Count of the number of tasks stop failures
     */
    public static final AtomicLong taskStoppedFailCount =  new AtomicLong(0L);

    /**
     * Jobs
     */

    /*
     * Count of jobs launched
     */
    public static final AtomicLong jobLaunchCount =  new AtomicLong(0L);

    /*
     * Count of jobs launched
     */
    public static final AtomicLong jobLaunchFailCount =  new AtomicLong(0L);

    /*
     * Count of jobs finished.
     */
    public static final AtomicLong jobFinishCount =  new AtomicLong(0L);

    /*
     * Count of jobs killed
     */
    public static final AtomicLong jobKillCount =  new AtomicLong(0L);

}
