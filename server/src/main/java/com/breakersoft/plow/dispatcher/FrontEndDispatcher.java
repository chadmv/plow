package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.command.DispatchCommand;
import com.breakersoft.plow.dispatcher.command.DispatchNodeCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.service.JobService;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.eventbus.Subscribe;

public final class FrontEndDispatcher {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(FrontEndDispatcher.class);

    @Autowired
    EventManager eventManager;

    @Autowired
    DispatchService dispatchService;

    @Autowired
    DispatchSupport dispatchSupport;

    @Autowired
    DispatchConfiguration dispatchConfig;

    @Autowired
    JobService jobService;

    private final List<BookingThread> bookingThreads;
    private final LinkedBlockingQueue<DispatchCommand> commandQueue;

    private final ConcurrentMap<UUID, DispatchFolder> folderIndex;
    private final ConcurrentMap<UUID, DispatchJob> jobIndex;

    public FrontEndDispatcher() {

        bookingThreads = Lists.newArrayListWithCapacity(
                Defaults.DISPATCH_BOOKING_THREADS);
        commandQueue = new LinkedBlockingQueue<DispatchCommand>();

        folderIndex = new MapMaker()
            .concurrencyLevel(Defaults.DISPATCH_BOOKING_THREADS)
            .weakKeys()
            .weakValues()
            .initialCapacity(100)
            .makeMap();

        jobIndex = new MapMaker()
            .concurrencyLevel(Defaults.DISPATCH_BOOKING_THREADS)
            .weakKeys()
            .weakValues()
            .initialCapacity(100)
            .makeMap();
    }

    @PostConstruct
    public void init() {
        eventManager.register(this);

        for (int i=0; i < Defaults.DISPATCH_BOOKING_THREADS; i++) {
            BookingThread thread = dispatchConfig.getBookingThread();
            bookingThreads.add(thread);
        }

        List<DispatchJob> jobs = dispatchService.getDispatchJobs();
        logger.info("Loading {} active jobs into dispatcher.", jobs.size());

        for (DispatchJob job: jobs) {
            addDispatchJob(job);
        }

        // Want to start these after jobs are added
        for (BookingThread thread: bookingThreads) {
            thread.start();
        }

    }

    public DispatchCommand getNextDispatchCommand() {
        try {
            return commandQueue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public void dispatch(Node node) {
        dispatch(dispatchService.getDispatchNode(node.getName()));
    }

    public void dispatch(DispatchNode node) {
        logger.info("Adding dispatch node: " + node.getName() + "to queue");
        commandQueue.offer(new DispatchNodeCommand(node));
        logger.info("Queue size :" + commandQueue.size());
    }

    public void dispatch(DispatchJob job, DispatchNode node) {
        logger.info("Dispatch Job " + job);
    }

    public void dispatch(DispatchLayer layer, DispatchNode node) {
        logger.info("Dispatch Job " + layer);
    }

    public void addDispatchJob(DispatchJob djob) {
        jobIndex.put(djob.getJobId(), djob);
        folderIndex.put(djob.getFolderId(),
                djob.getFolder());

        for (BookingThread thread: bookingThreads) {
            thread.addJob(djob);
        }
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        logger.info("handling job launch event");
        addDispatchJob(dispatchService.getDispatchJob(event));
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        logger.info("Job shutdown event");
    }
}
