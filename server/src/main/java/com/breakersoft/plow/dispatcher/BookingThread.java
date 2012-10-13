package com.breakersoft.plow.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.dispatcher.command.DispatchCommand;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchLayer;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchProject;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.exceptions.DispatchProcAllocationException;
import com.breakersoft.plow.exceptions.RndClientExecuteException;
import com.breakersoft.plow.service.JobService;

import com.google.common.collect.Maps;

/**
 * This thread will contain a copy of the entire layer list.
 * It will sort it, iterate it, come up with a list of commands to execute
 * Then pass those commands off the dispatch executor pool.
 * The dispatch thread never goes to the network.
 *
 * @author chambers
 *
 */
public class BookingThread extends Thread {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(BookingThread.class);

    private static final int EXPECTED_PROJECT_COUNT = 16;

    @Autowired
    private FrontEndDispatcher dispatcher;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private JobService jobService;

    @Autowired
    private DispatchSupport dispatchSupport;

    private boolean enabled = true;
    private final Map<UUID, ArrayList<DispatchJob>> activeJobs;
    private final ConcurrentLinkedQueue<DispatchJob> newJobs;

    public BookingThread() {
        logger.info("booking thread initialized.");
        newJobs = new ConcurrentLinkedQueue<DispatchJob>();
        activeJobs = Maps.newHashMapWithExpectedSize(EXPECTED_PROJECT_COUNT);
    }

    @Override
    public void run() {

        while(enabled) {

            // Pull a node out of the dispatcher queue.
            DispatchCommand command = dispatcher.getNextDispatchCommand();

            // The thread was interrupted?  Not sure if a null
            // is the best way to handle it or if we should
            // catch it here.
            if (command == null) {
                logger.info("getNextDispatchNode returned null, exiting the dispatch thread.");
                return;
            }

            logger.info("Dispatcher thread picked up command");
            command.execute(this);
        }
    }

    public void book(DispatchNode node) {

        logger.info("Dispatching: " + node.getName());

        /*
         * Grab a list of projects sorted by farthest from
         * their quota for the node's cluster.
         */
        final List<DispatchProject> projects =
                dispatchService.getSortedProjectList(node);

        if (projects.isEmpty()) {
            logger.info("No bookable projects!");
        }

        for (DispatchProject project: projects) {

            final UUID id = project.getProjectId();

            // Skip over project if there are no active jobs.
            if (!activeJobs.containsKey(id)) {
                logger.info("No active jobs in project: " + id);
                continue;
            }

            // Skip over project if there are no active jobs.
            if (activeJobs.get(id).size() == 0) {
                logger.info("No active jobs in project: " + id);
                continue;
            }

            if (!node.isDispatchable()) {
                return;
            }

            book(node, project);
        }
    }

    public void book(DispatchNode node, DispatchProject project) {

        UUID projId = project.getProjectId();

        // Sort the job list for the current project.
        logger.info("Sorting jobs for project {}", projId);
        Collections.sort(activeJobs.get(projId));

        for (DispatchJob job: activeJobs.get(projId)) {

             if (job.getWaitingFrames() == 0) {
                 logger.info("{} has no pending frames.", job.getName());
                 continue;
             }

             if (!node.isDispatchable()) {
                 logger.info("{} is no longer dispatchable.", node.getName());
                 return;
             }

             logger.info("{} is up for dispatch.", job.getName());
             book(node, job);
         }
    }

    public void book(DispatchNode node, DispatchJob job) {
        for (DispatchLayer layer:
            dispatchService.getDispatchLayers(job, node)) {
                if (!node.isDispatchable()) {
                    logger.info("{} is no longer dispatchable.", node.getName());
                    return;
                }
                logger.info("booking layer: {}", layer.getLayerId());
                book(node, layer);
        }
    }

    public void book(DispatchNode node, DispatchLayer layer) {
        for (DispatchTask task:
            dispatchService.getDispatchTasks(layer, node)) {

            if(!dispatchSupport.canDispatch(task, node)) {
                logger.info("{} is no longer dispatchable.", node.getName());
                break;
            }

            logger.info("booking task: {}", task.getName());
            book(node, task);
        }
    }

    public void book(DispatchNode node, DispatchTask task) {

        if (!dispatchService.reserveTask(task)) {
            logger.warn("Unable to reserve task: {}", task.getName());
            return;
        }

        DispatchProc proc = null;
        try {
            proc = dispatchService.allocateDispatchProc(node, task);
            if (jobService.startTask(task, proc)) {
                dispatchSupport.runRndTask(task, proc);
            }
        }
        catch (DispatchProcAllocationException e) {
            /*
             * Proc was not able to be allocated from the database.
             * This usually occurs when another thread is working
             * on the same host, so its best to just stop dispatching.
             */
            logger.warn("Failed to allocate {}/{} from {}",
                    new Object[] { task.getMinCores(), task.getMinMemory(), node.getName()});
            node.setDispatchable(false);

        } catch (RndClientExecuteException e) {
            /*
             * Unable to talk to host.
             */
            logger.warn("Failed to execute task on: {} " + node.getName());
            dispatchService.cleanupFailedDispatch(proc);
            node.setDispatchable(false);
        }
        catch (Exception e) {
            /*
             * Some unexpected exception we didn't think of.
             */
            logger.warn("Unexpected task dipatching error, " + e);
            jobService.unreserveTask(task);
            dispatchService.cleanupFailedDispatch(proc);
            node.setDispatchable(false);
        }
    }

    public void addJob(DispatchJob job) {
        newJobs.add(job);
    }

    public void update() {
        logger.info("Updating!");
        addNewJobs();
    }

    private void addNewJobs() {

        int count = 0;
        while (true) {

            DispatchJob job = newJobs.poll();
            if (job == null) {
                logger.info("No new jobs waiting to be ingested.");
                break;
            }
            count++;

            if (!activeJobs.containsKey(job.getProjectId())) {
                activeJobs.put(job.getProjectId(),
                        new ArrayList<DispatchJob>());
            }
            activeJobs.get(job.getProjectId()).add(job);
        }

        logger.info("Added " + count + " jobs to the dispatcher.");
    }

    public int getWaitingJobs() {
        return newJobs.size();
    }

}
