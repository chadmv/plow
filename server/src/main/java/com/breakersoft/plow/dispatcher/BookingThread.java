package com.breakersoft.plow.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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
public class BookingThread implements Runnable {

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
            DispatchNode node = dispatcher.getNextDispatchNode();

            // The thread was interrupted?  Not sure if a null
            // is the best way to handle it or if we should
            // catch it here.
            if (node == null) {
                logger.info("getNextDispatchNode returned null, exiting the dispatch thread.");
                return;
            }

            book(node);
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

        // Update our job list with waiting data.
        update();

        for (DispatchProject project: projects) {

            final UUID id = project.getProjectId();

            // Skip over project if there are no active jobs.
            if (!activeJobs.containsKey(id)) {
                continue;
            }

            // Skip over project if there are no active jobs.
            if (activeJobs.get(id).size() == 0) {
                continue;
            }

            // Sort the job list for the current project.
            Collections.sort(activeJobs.get(id));

            for (DispatchJob job: activeJobs.get(id)) {

                logger.info(job + " is up for dispatch.");

                if (job.getWaitingFrames() == 0) {
                    logger.info(job + " has no pending frames.");
                    continue;
                }

                for (DispatchLayer layer:
                    dispatchService.getDispatchLayers(job, node)) {

                    logger.info("Dispatching layer " + layer.toString());

                    for (DispatchTask task:
                        dispatchService.getDispatchTasks(layer, node)) {

                        if(!dispatchSupport.canDispatch(task, node)) {
                            break;
                        }
                        DispatchProc proc = null;
                        try {
                            proc = dispatchService.allocateDispatchProc(node, project, task);
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
                }
            }
        }

        logger.info("Dispatch complete.");
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
