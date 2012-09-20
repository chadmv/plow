package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobShutdownEvent;
import com.breakersoft.plow.service.DispatcherService;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

@Component
public final class ThreadedDispatcherImpl implements Dispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(ThreadedDispatcherImpl.class);

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatcherService dispatcherService;

    private int numThreads = 4;
    private final List<DispatcherThread> threads;
    private LinkedBlockingQueue<DispatchNode> nodeQueue;

    public ThreadedDispatcherImpl() {
        nodeQueue = new LinkedBlockingQueue<DispatchNode>();
        threads = Lists.newArrayList();
        for (int i=0; i<numThreads; i++) {
            threads.add(new DispatcherThread(this, dispatcherService));
        }
    }

    @PostConstruct
    public void init() {
        eventManager.register(this);
    }

    @Override
    public DispatchNode getNextDispatchNode() {
        return nodeQueue.poll();
    }

    @Override
    public void dispatch(DispatchNode node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispatch(DispatchJob job, DispatchNode node) {
        // TODO Auto-generated method stub
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        logger.info("Job launch event!");
    }

    @Subscribe
    public void handleJobShutdownEvent(JobShutdownEvent event) {
        logger.info("Job shutdown event");
    }
}
