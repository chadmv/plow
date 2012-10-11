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
import com.breakersoft.plow.exceptions.RndClientConnectionError;
import com.breakersoft.plow.exceptions.RndClientExecuteException;

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
    private DispatchSupport dispatchSupport;

    private boolean doReload = true;
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

            book(node, project);
        }
    }

    public void book(DispatchNode node, DispatchProject project) {

        UUID projId = project.getProjectId();

        // Sort the job list for the current project.
        logger.info("Soring jobs for project " + projId);
        Collections.sort(activeJobs.get(projId));

        for (DispatchJob job: activeJobs.get(projId)) {

             if (job.getWaitingFrames() == 0) {
                 logger.info(job + " has no pending frames.");
                 continue;
             }

             logger.info(job + " is up for dispatch.");
             book(node, job);
         }
    }

    public void book(DispatchNode node, DispatchJob job) {
        for (DispatchLayer layer:
            dispatchService.getDispatchLayers(job, node)) {
              book(node, layer);
        }
    }

    public void book(DispatchNode node, DispatchLayer layer) {
        for (DispatchTask task:
            dispatchService.getDispatchTasks(layer, node)) {

            if(!dispatchSupport.canDispatch(task, node)) {
                break;
            }
            book(node, task);
        }
    }

    public void book(DispatchNode node, DispatchTask task) {

        DispatchProc proc = null;
        try {
            proc = dispatchService.allocateDispatchProc(node, task);
            dispatchSupport.runRndTask(task, proc);

            if (node.getCores() == 0) {
                return;
            }
        } catch (RndClientConnectionError e) {
            // RND Client is down.
            logger.warn("RND node is down " + node.getName() + ", " + e);
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
            e.printStackTrace();
            return;
        }
    }


    public void addJob(DispatchJob job) {
        logger.info("Adding new job to newJobs list");
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
                break;
            }
            count++;

            if (!activeJobs.containsKey(job.getProjectId())) {
                activeJobs.put(job.getProjectId(),
                        new ArrayList<DispatchJob>(100));
            }
            activeJobs.get(job.getProjectId()).add(job);
        }

        logger.info("Added " + count + " jobs to the dispatcher.");
    }

    public int getWaitingJobs() {
        return newJobs.size();
    }

}
