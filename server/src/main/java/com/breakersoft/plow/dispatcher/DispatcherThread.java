package com.breakersoft.plow.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.service.DispatcherService;
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

    private static final int EXPECTED_PROJECT_COUNT = 16;
    private static final int EXPECTED_FOLDER_COUNT = 128;
    private static final int EXPECTED_JOB_COUNT = 512;
    private static final int EXPECTED_LAYER_COUNT = 2048;

    private boolean enabled = true;

    private final Dispatcher dispatcher;
    private final DispatcherService dispatcherService;
    private boolean doReload = true;

    ConcurrentMap<UUID, DispatchFolder> folderIndex;
    ConcurrentMap<UUID, DispatchJob> jobIndex;
    ConcurrentMap<UUID, DispatchLayer> layerIndex;

    Map<Project, ArrayList<DispatchLayer>> currentLayers;

    ArrayList<DispatchLayer> newLayers;
    ArrayList<DispatchLayer> oldLayers;

    public DispatcherThread(Dispatcher dispatcher, DispatcherService service) {
        this.dispatcher = dispatcher;
        this.dispatcherService = service;

        folderIndex = new MapMaker()
            .concurrencyLevel(1)
            .initialCapacity(EXPECTED_FOLDER_COUNT)
            .weakValues()
            .makeMap();

        jobIndex = new MapMaker()
            .concurrencyLevel(1)
            .initialCapacity(EXPECTED_JOB_COUNT)
            .weakValues()
            .makeMap();

        layerIndex = new MapMaker()
            .concurrencyLevel(1)
            .initialCapacity(EXPECTED_LAYER_COUNT)
            .weakValues()
            .makeMap();

        currentLayers =
                Maps.newHashMapWithExpectedSize(
                EXPECTED_PROJECT_COUNT);
    }

    @Override
    public void run() {

        while(enabled) {

            update();

            // Pull a node out of the dispatcher queue.
            DispatchNode node = dispatcher.getNextDispatchNode();

            // Pull the sorted show list based on priority
            // for the given node.
            List<Project> projects = dispatcherService.getSortedProjectList(node);

            for (Project project: projects) {

                DispatchJob dispatchedJob = null;
                UUID id = project.getProjectId();

                Collections.sort(currentLayers.get(id));

                for (DispatchLayer layer: currentLayers.get(id)) {

                    // Don't dispatch the last job again.
                    if (layer.getJob().equals(dispatchedJob)) {
                        continue;
                    }

                    // Make sure we can dispatch from at
                    // least a single layer.
                    if (!layer.canDispatch(node)) {
                        continue;
                    }

                    dispatcher.dispatch(layer.getJob(), node);
                    dispatchedJob = layer.getJob();
                }
            }
        }

        // TODO Auto-generated method stub

    }

    public void addDispatchableLayers(List<DispatchLayer> layers) {
        newLayers.addAll(layers);
    }

    public void removeDispatchableLayers(List<DispatchLayer> layers) {
        oldLayers.addAll(layers);
    }

    private void update() {
        __removeLayers();
        __addLayers();
    }

    private void __removeLayers() {
        for (DispatchLayer layer: oldLayers) {
            UUID project = layer.getFolder().getProjectId();
            currentLayers.get(project).remove(layer);
        }
        System.gc();
    }

    private void __addLayers() {
        for (DispatchLayer layer: newLayers) {
            currentLayers.get(layer.getFolder().getProjectId()).add(layer);

            DispatchFolder folder = layer.getFolder();
            DispatchJob job = layer.getJob();

            folderIndex.putIfAbsent(folder.getFolderId(), folder);
            jobIndex.putIfAbsent(job.getJobId(), job);
            layerIndex.putIfAbsent(layer.getLayerId(), layer);
        }
    }

}
