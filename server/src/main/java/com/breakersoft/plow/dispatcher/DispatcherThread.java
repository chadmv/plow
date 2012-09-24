package com.breakersoft.plow.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.DispatchDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.rnd.RndClient;
import com.breakersoft.plow.rnd.RndClientException;
import com.breakersoft.plow.rnd.thrift.RunProcessCommand;
import com.breakersoft.plow.service.DispatcherService;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
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
public class DispatcherThread implements Runnable {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatcherThread.class);

    private static final int EXPECTED_PROJECT_COUNT = 16;

    private boolean enabled = true;

    private final Dispatcher dispatcher;
    private final DispatcherService dispatcherService;
    private boolean doReload = true;

    private final Map<UUID, ArrayList<DispatchJob>> activeJobs;
    private final ConcurrentLinkedQueue<DispatchJob> newJobs;

    public DispatcherThread(Dispatcher dispatcher, DispatcherService service) {
        this.dispatcher = dispatcher;
        this.dispatcherService = service;

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
        }
        // TODO Auto-generated method stub
    }

    public void dispatch(DispatchNode node) {

        logger.info("Dispatching: " + node.getName());

        // Pull the sorted show list based on priority
        // for the given node.
        final List<DispatchProject> projects =
                dispatcherService.getSortedProjectList(node);

        update();

        for (DispatchProject project: projects) {

            final UUID id = project.getProjectId();

            if (!activeJobs.containsKey(id)) {
                continue;
            }

            if (activeJobs.get(id).size() == 0) {
                continue;
            }

            logger.info("Dispatching project: " + project.getProjectId());
            Collections.sort(activeJobs.get(id));

            for (DispatchJob job: activeJobs.get(id)) {

                logger.info(job + " is up for dispatch.");

                if (job.getWaitingFrames() == 0) {
                    logger.info(job + " has no pending frames.");
                    continue;
                }

                List<DispatchLayer> layers = Lists.newArrayList();
                for (DispatchLayer layer: job.getLayers()) {

                    if (layer.getWaitingFrames() == 0) {
                        logger.info(layer + " has no pending frames.");
                        continue;
                    }

                    if (dispatcher.canDispatch(layer, node)) {
                        logger.info("Can dispatch " + layer);
                        layers.add(layer);
                    }
                    else {
                        logger.info("Cannot dispatch " + layer);
                    }
                }

                logger.info("Layers to dipsatch " + layers.size());

                if (layers.size() == 0) {
                    break;
                }

                /**
                 * This should be better.
                 */
                Collections.sort(layers, new Comparator<DispatchLayer>() {
                    @Override
                    public int compare(DispatchLayer o1, DispatchLayer o2) {
                        return ComparisonChain.start()
                                .compare(o2.getWaitingFrames(), o1.getWaitingFrames())
                                .result();
                    }
                });

                for (DispatchLayer layer: layers) {

                    List<DispatchTask> tasks =
                            dispatcherService.getTasks(layer, node);

                    for (DispatchTask task: tasks) {

                        if(!dispatcher.canDispatch(layer, node)) {
                            break;
                        }

                        DispatchProc proc = new DispatchProc();
                        proc.setFrameId(task.getTaskId());
                        proc.setNodeId(node.getNodeId());
                        proc.setQuotaId(project.getQuotaId());
                        proc.setProcId(UUID.randomUUID());
                        proc.setCores(layer.getMinCores());
                        proc.setFrameName(task.getName());
                        proc.setNumber(task.getNumber());

                        try {

                            dispatcherService.createDispatchProc(proc);

                            node.setIdleCores(node.getIdleCores() - layer.getMinCores());
                            node.setIdleMemory(node.getIdleMemory() - layer.getMinMemory());
                            runProcess(task, proc, node);

                        } catch (RndClientException e) {
                            logger.info("RndClientException, " + e);
                            return;
                        }
                        catch (Exception e) {
                            //
                        }
                    }
                }
            }
        }

        logger.info("Dispatch complete.");
    }

    private void runProcess(DispatchTask task, DispatchProc proc, DispatchNode node) {

        RunProcessCommand cmd = new RunProcessCommand();
        cmd.command = Arrays.asList(task.getCommand());
        cmd.cores = proc.getCores();
        cmd.env = Maps.newHashMap();
        cmd.frameId = task.getTaskId().toString();
        cmd.procId = proc.getProcId().toString();
        cmd.logFile = String.format("/tmp/%s.log", task.getName());

        RndClient client = new RndClient(node.getName(), 11338);
        client.runProcess(cmd);
        logger.info("process is running");
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
