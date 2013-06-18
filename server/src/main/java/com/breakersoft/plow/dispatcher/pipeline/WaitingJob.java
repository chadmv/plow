package com.breakersoft.plow.dispatcher.pipeline;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class WaitingJob {

    private UUID jobId;
    private AtomicInteger count;

    public WaitingJob(UUID jobId) {
        this.jobId = jobId;
        this.count = new AtomicInteger(0);
    }

    public void increasePriority() {
        this.count.incrementAndGet();
    }

    public int getPriority() {
        return count.get();
    }



}
