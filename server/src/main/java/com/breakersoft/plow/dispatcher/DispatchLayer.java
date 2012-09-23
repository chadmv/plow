package com.breakersoft.plow.dispatcher;

import java.util.Set;

import org.slf4j.Logger;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;

public final class DispatchLayer extends LayerE {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchLayer.class);

    private int minCores;
    private int maxCores;
    private int minMemory;
    private int waitingFrames;

    private Set<String> tags;

    public DispatchLayer() { }

    public DispatchLayer(Layer layer) {
        this.setJobId(layer.getJobId());
        this.setLayerId(layer.getLayerId());
    }

    public int getMinCores() {
        return minCores;
    }

    public void setMinCores(int cores) {
        this.minCores = cores;
    }

    public int getMaxCores() {
        return maxCores;
    }

    public void setMaxCores(int maxCores) {
        this.maxCores = maxCores;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

    public int getWaitingFrames() {
        return waitingFrames;
    }

    public void setWaitingFrames(int waitingFrames) {
        this.waitingFrames = waitingFrames;
    }
}
