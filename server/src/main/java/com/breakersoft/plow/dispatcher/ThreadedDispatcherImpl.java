package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.service.DispatcherService;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
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

    private ConcurrentMap<UUID, DispatchFolder> folderIndex;
    private ConcurrentMap<UUID, DispatchJob> jobIndex;
    private ConcurrentMap<UUID, DispatchLayer> layerIndex;

    public ThreadedDispatcherImpl() {
        nodeQueue = new LinkedBlockingQueue<DispatchNode>();
        threads = Lists.newArrayList();
        for (int i=0; i<numThreads; i++) {
            threads.add(new DispatcherThread(this, dispatcherService));
        }

        folderIndex = new MapMaker()
            .concurrencyLevel(numThreads)
            .weakKeys()
            .weakValues()
            .initialCapacity(100)
            .makeMap();

        jobIndex = new MapMaker()
            .concurrencyLevel(numThreads)
            .weakKeys()
            .weakValues()
            .initialCapacity(100)
            .makeMap();

        layerIndex = new MapMaker()
            .concurrencyLevel(numThreads)
            .weakKeys()
            .weakValues()
            .initialCapacity(100)
            .makeMap();
    }

    @PostConstruct
    public void init() {
        eventManager.register(this);
    }

    @Override
    public DispatchNode getNextDispatchNode() {
        try {
            return nodeQueue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    @Override
    public void dispatch(DispatchNode node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void dispatch(DispatchJob job, DispatchNode node) {
        logger.info("Dispatch Job " + job);
    }


    @Override
    public void dispatch(DispatchLayer layer, DispatchNode node) {
        logger.info("Dispatch Job " + layer);




    }

    @Override
    public boolean canDispatch(DispatchLayer layer, DispatchNode node) {

        logger.info("Node Cores: " + node.getIdleCores() + " Layer Cores: " + layer.getMinCores());
        logger.info("Node Mem: " + node.getIdleMemory() + " Layer Cores: " + layer.getMinMemory());
        if (node.getIdleMemory() < layer.getMinMemory()) {

            return false;
        }

        if (node.getIdleCores() < layer.getMinCores()) {
            return false;
        }

        return true;
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {

        // Index all the job data.
        DispatchFolder dfolder = dispatcherService.getDispatchFolder(event.getFolder());
        DispatchJob djob = dispatcherService.getDispatchJob(event.getJob());

        djob.setFolder(dfolder);
        djob.setLayers(dispatcherService.getDispatchLayers(djob));

        jobIndex.put(djob.getJobId(), djob);
        folderIndex.put(dfolder.getFolderId(), dfolder);

        for (DispatcherThread thread: threads) {
            thread.addJob(djob);
        }
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        logger.info("Job shutdown event");
    }
}
