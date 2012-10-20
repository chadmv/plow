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
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dispatcher.command.BookingCommand;
import com.breakersoft.plow.dispatcher.command.BookNode;
import com.breakersoft.plow.dispatcher.domain.DispatchFolder;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.event.JobFinishedEvent;
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
    private final LinkedBlockingQueue<BookingCommand> bookingQueue;

    private final ConcurrentMap<UUID, DispatchFolder> folderIndex;
    private final ConcurrentMap<UUID, DispatchJob> jobIndex;

    public FrontEndDispatcher() {

        bookingThreads = Lists.newArrayListWithCapacity(
                Defaults.DISPATCH_BOOKING_THREADS);
        bookingQueue = new LinkedBlockingQueue<BookingCommand>();

        folderIndex = new MapMaker()
            .concurrencyLevel(Defaults.DISPATCH_BOOKING_THREADS)
            .initialCapacity(100)
            .makeMap();

        jobIndex = new MapMaker()
            .concurrencyLevel(Defaults.DISPATCH_BOOKING_THREADS)
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
            addJob(job);
        }

        // Want to start these after jobs are added
        for (BookingThread thread: bookingThreads) {
            thread.start();
        }
    }

    public BookingCommand getNextBookingCommand() {
        try {
            return bookingQueue.take();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public void book(Node node) {
        book(dispatchService.getDispatchNode(node.getName()));
    }

    public void book(DispatchNode node) {
        bookingQueue.offer(new BookNode(node));
    }

    public void book(DispatchNode node, DispatchJob job) {
        logger.info("Dispatch Job " + job);
    }

    public void book(DispatchNode node, DispatchLayer layer) {
        logger.info("Dispatch Job " + layer);
    }

    public boolean dispatch(DispatchProc proc, DispatchJob job) {

        logger.info("Dipatching proc: {}", proc);

        final List<DispatchLayer> layers =
                dispatchService.getDispatchLayers(job, proc);

        logger.info("Dispatching job: {}, matching layers: {}",
                job.getJobId(), layers.size());

        for (DispatchLayer layer: layers) {
            logger.info("booking layer: {}", layer.getLayerId());
            boolean result = dispatch(proc, layer);
            if (result) {
                return true;
            }
        }
        dispatchService.unbookProc(proc);
        return false;
    }

    public boolean dispatch(DispatchProc proc, DispatchLayer layer) {

        final List<DispatchTask> tasks =
                dispatchService.getDispatchTasks(layer, proc);

        logger.info("Dispatching layer: {}, matching tasks: {}",
                layer.getLayerId(), tasks.size());

        for (DispatchTask task: tasks) {
            logger.info("booking task: {}", task.getTaskId());
            boolean result = dispatch(proc, task);
            if (result) {
                return true;
            }
        }

        dispatchService.unbookProc(proc);
        return false;
    }

    public boolean dispatch(DispatchProc proc, DispatchTask task) {

        if (!dispatchService.reserveTask(task)) {
            logger.warn("Unable to reserve task: {}", task.getName());
            return false;
        }

        try {
            dispatchService.assignProc(proc, task);
            if (jobService.startTask(task, proc)) {
                dispatchSupport.runRndTask(task, proc);
                return true;
            }
        } catch (RndClientExecuteException e) {
            /*
             * Unable to talk to host.
             */
            logger.warn("Failed to execute task on: {} " + proc.getNodeName());
            jobService.unreserveTask(task);
            dispatchService.unbookProc(proc);
        }
        catch (Exception e) {
            /*
             * Some unexpected exception we didn't think of.
             */
            logger.warn("Unexpected task dipatching error, " + e);
            jobService.unreserveTask(task);
            dispatchService.unbookProc(proc);
        }

        return false;
    }

    public DispatchJob getJob(UUID id) {
        return jobIndex.get(id);
    }

    public void addJob(DispatchJob djob) {
        logger.info("Adding dispatch job: {}", djob.getJobId());
        jobIndex.put(djob.getJobId(), djob);

        DispatchFolder folder;
        if (folderIndex.containsKey(djob.getFolderId())) {
            folder = folderIndex.get(djob.getFolderId());
        }
        else {
            folder = folderIndex.putIfAbsent(djob.getFolderId(),
                    dispatchService.getDispatchFolder(djob.getFolderId()));
            if (folder == null) {
                folder = folderIndex.get(djob.getFolderId());
            }
        }
        djob.setFolder(folder);

        for (BookingThread thread: bookingThreads) {
            thread.addJob(djob);
        }
    }

    public void removeJob(Job job) {
        for (BookingThread thread: bookingThreads) {
            thread.removeJob(job);
        }

        DispatchJob djob = jobIndex.get(job.getJobId());
        djob.setFolder(null);

        jobIndex.remove(job.getJobId());
    }

    public int getTotalJobs() {
        return jobIndex.size();
    }

    public int getTotalFolders() {
        return folderIndex.size();
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        logger.info("handling job launch event");
        addJob(dispatchService.getDispatchJob(event));
    }

    @Subscribe
    public void handleJobShutdownEvent(JobFinishedEvent event) {
        logger.info("hanlding job shutdown event");
        removeJob(event.getJob());
    }
}
