package com.breakersoft.plow.dispatcher.domain;

import java.util.List;

import com.breakersoft.plow.JobE;
import com.breakersoft.plow.dispatcher.Dispatchable;
import com.google.common.collect.ComparisonChain;

public final class DispatchJob extends JobE implements Dispatchable, Comparable<DispatchJob> {

    private int minCores;
    private int maxCores;
    private int runCores;
    private int waitingFrames;
    private String name;

    private float tier;

    private List<DispatchLayer> layers;
    private DispatchFolder folder;

    public DispatchJob() {
        tier = 0.0f;
    }

    public float getTier() {
        return tier;
    }

    @Override
    public boolean isDispatchable() {
        return true;
    }

    public void setMinCores(int cores) {
        this.minCores = cores;
        recalculate();
    }

    public void setMaxCores(int cores) {
        this.minCores = cores;
    }

    public int getMaxCores() {
        return runCores;
    }

    public int getRunCores() {
        return runCores;
    }

    @Override
    public void incrementCores(int inc) {
        runCores = runCores + inc;
        recalculate();
    }

    @Override
    public void decrementCores(int dec) {
        runCores = runCores - dec;
        recalculate();
    }

    @Override
    public void recalculate() {
        if (minCores <= 0) {
            tier = runCores;
        }
        else {
            tier = runCores / minCores;
        }
    }

    public DispatchFolder getFolder() {
        return folder;
    }

    public void setFolder(DispatchFolder folder) {
        this.folder = folder;
    }

    public List<DispatchLayer> getDispatchLayers() {
        return layers;
    }

    public void setLayers(List<DispatchLayer> layers) {
        this.layers = layers;
    }

    @Override
    public int compareTo(DispatchJob other) {
        return ComparisonChain.start()
                .compare(folder.getTier(), other.getFolder().getTier())
                .compare(getTier(), getTier())
                .result();
    }

    public int getWaitingFrames() {
        return waitingFrames;
    }

    public void setWaitingFrames(int waitingFrames) {
        this.waitingFrames = waitingFrames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
