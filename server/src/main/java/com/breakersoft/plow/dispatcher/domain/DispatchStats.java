package com.breakersoft.plow.dispatcher.domain;

import java.util.concurrent.atomic.AtomicLong;

public class DispatchStats {

    public static final AtomicLong totalDispatchCount = new AtomicLong(0L);

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
