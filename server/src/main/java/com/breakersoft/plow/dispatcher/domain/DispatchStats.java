package com.breakersoft.plow.dispatcher.domain;

import java.util.concurrent.atomic.AtomicLong;

public class DispatchStats {

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

    public static final AtomicLong taskReservedCount = new AtomicLong(0L);
    public static final AtomicLong taskReservedCollisionCount = new AtomicLong(0L);
    public static final AtomicLong taskStartedCount = new AtomicLong(0L);
    public static final AtomicLong taskStoppedCount = new AtomicLong(0L);
    public static final AtomicLong taskStartedFailureCount = new AtomicLong(0L);
    public static final AtomicLong taskStoppedFailureCount = new AtomicLong(0L);


    public static final AtomicLong procAllocCount = new AtomicLong(0L);
    public static final AtomicLong procAllocFailedCount = new AtomicLong(0L);
    public static final AtomicLong procDeallocCount = new AtomicLong(0L);
    public static final AtomicLong rndCommFailedCount = new AtomicLong(0L);

}
