package com.breakersoft.plow.dispatcher;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobFinishedEvent;
import com.breakersoft.plow.exceptions.DispatchProcAllocationException;
import com.breakersoft.plow.exceptions.RndClientConnectionError;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
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
    private final LinkedBlockingQueue<DispatchNode> nodeQueue;

    private final ConcurrentMap<UUID, DispatchFolder> folderIndex;
    private final ConcurrentMap<UUID, DispatchJob> jobIndex;

    public FrontEndDispatcher() {

        bookingThreads = Lists.newArrayListWithCapacity(
                Defaults.DISPATCH_BOOKING_THREADS);
        nodeQueue = new LinkedBlockingQueue<DispatchNode>();

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
        for (int i=0; i < Defaults.DISPATCH_BOOKING_THREADS; i++) {
            bookingThreads.add(dispatchConfig.getBookingThread());
        }
        eventManager.register(this);
    }

    public void addBookingThread(BookingThread thread) {
        bookingThreads.add(thread);
    }

    public DispatchNode getNextDispatchNode() {
        try {
            return nodeQueue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public void dispatch(DispatchNode node) {
        // TODO Auto-generated method stub
    }

    public void dispatch(DispatchJob job, DispatchNode node) {
        logger.info("Dispatch Job " + job);
    }

    public void dispatch(Job job, DispatchProc proc) {

        logger.info("Dispatch Job " + job);

        for (DispatchLayer layer:
            dispatchService.getDispatchLayers(job, proc)) {

            logger.info("Dispatching layer " + layer.toString());

            for (DispatchTask task:
                dispatchService.getDispatchTasks(layer, proc)) {

                try {
                    dispatchSupport.runRndTask(task, proc);
                } catch (RndClientConnectionError e) {
                    // RND Client is down.
                    logger.warn("RND node is down " + proc.getNodeName() + ", " + e);
                    dispatchService.cleanupFailedDispatch(proc);
                    //TODO: need to lock out host;
                    return;
                } catch (RndClientExecuteException e) {
                    logger.warn("RND exception " + e);
                    dispatchService.cleanupFailedDispatch(proc);
                    return;
                }
                catch (DispatchProcAllocationException e) {
                    // Failed to allocate the proc
                    logger.warn("Failed to allocation cores from proc.");
                    //no need to clean
                    return;
                }
                catch (Exception e) {
                    logger.warn("catch all exception " + e);
                    return;
                }
            }
        }
    }

    public void dispatch(DispatchLayer layer, DispatchNode node) {
        logger.info("Dispatch Job " + layer);
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        DispatchJob djob = dispatchService.getDispatchJob(event);
        jobIndex.put(djob.getJobId(), djob);
        folderIndex.put(event.getFolder().getFolderId(),
                djob.getFolder());

        for (BookingThread thread: bookingThreads) {
            thread.addJob(djob);
        }
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        logger.info("Job shutdown event");
    }
}
