package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import com.breakersoft.plow.service.DispatcherService;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

@Component
public final class ThreadedDispatcherImpl implements Dispatcher {

    private DispatcherService dispatcherService;

    private int numThreads = 4;
    private final List<DispatcherThread> threads;

    private LinkedBlockingQueue<DispatchNode> nodeQueue;

    public ThreadedDispatcherImpl() {
        threads = Lists.newArrayList();
        for (int i=0; i<numThreads; i++) {
            threads.add(new DispatcherThread(this, dispatcherService));
        }
    }

    @Subscribe
    public void handleJobLaunchEvent() {
        // Load in the job and add it to each thread.
    }

    @Subscribe
    public void handleJobShutdownEvent() {
        // Remove job from all threads
    }

    @Subscribe
    public void handleBookingEvent() {
        // Remove job from all threads
    }

    @Subscribe
    public void handleUnbookingEvent() {
        // Remove job from all threads
    }

    @Subscribe
    public void handleReloadEvent() {
        // Pull all jobs from DB and copy into each thread.
    }

    public void handleDispatchStateEvent() {
        // Changes the dispatchable state of a layer, folder, or job.

    }

    @Override
    public DispatchNode getNextDispatchNode() {
        return nodeQueue.poll();
    }

    @Override
    public void dispatch(DispatchJob job, DispatchNode node) {

    }

}
