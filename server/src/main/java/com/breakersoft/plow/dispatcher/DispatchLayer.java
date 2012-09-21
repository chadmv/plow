package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.LayerE;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;

public final class DispatchLayer extends LayerE implements Dispatchable, Comparable<DispatchLayer> {

    private int minCores;
    private int maxCores;
    private int runCores;
    private int minMemory;
    private float tier = 0.0f;

    private DispatchJob job;
    private DispatchFolder folder;
    private boolean isDispatchable;

    private ImmutableSet<String> tags;

    public DispatchLayer() { }

    public DispatchLayer(Layer layer) {
        this.setJobId(layer.getJobId());
        this.setLayerId(this.getLayerId());
    }

    public boolean canDispatch(DispatchNode node) {

        if (!folder.isDispatchable()) {
            return false;
        }

        if (!job.isDispatchable()) {
            return false;
        }

        if (!isDispatchable()) {
            return false;
        }

        if (!isDispatchable()) {
            return false;
        }

        if (node.getIdleMemory() < minMemory) {
            return false;
        }

        if (node.getIdleCores() < minCores) {
            return false;
        }

        return true;
    }

    public DispatchFolder getFolder() {
        return folder;
    }

    public DispatchJob getJob() {
        return job;
    }

    public float getTier() {
        return tier;
    }

    @Override
    public boolean isDispatchable() {
        if (runCores >= maxCores) {
            return false;
        }
        return isDispatchable;
    }

    @Override
    public void incrementCores(int cores) {
        this.runCores = this.runCores + cores;
        this.recalculate();
    }

    @Override
    public void decrementCores(int cores) {
        this.runCores = this.runCores - cores;
        this.recalculate();
    }

    public int getMinCores() {
        return minCores;
    }

    public void setMinCores(int minCores) {
        this.minCores = minCores;
        this.recalculate();
    }

    public int getMaxCores() {
        return maxCores;
    }

    public void setMaxCores(int maxCores) {
        this.maxCores = maxCores;
    }

    public ImmutableSet<String> getTags() {
        return tags;
    }

    public void setTags(ImmutableSet<String> tags) {
        this.tags = tags;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

     public void setJob(DispatchJob job) {
         this.job = job;
     }

     public void setFolder(DispatchFolder folder) {
         this.folder = folder;
     }

     public void setDispatchable(boolean isDispatchable) {
         this.isDispatchable = isDispatchable;
     }

     public void recalculate() {
         if (minCores <= 0) {
             tier = runCores;
         }
         else {
             tier = runCores / (float) minCores;
         }
     }

     @Override
     public int compareTo(DispatchLayer other) {
         return ComparisonChain.start()
                 .compare(this.folder.getTier(), other.getFolder().getTier())
                 .compare(this.job.getTier(), other.getJob().getTier())
                 .compare(this.tier, other.getTier())
                 .result();
     }

}
