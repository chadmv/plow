package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.JobE;

public final class DispatchJob extends JobE implements Dispatchable {

    private int minCores;
    private int maxCores;
    private int runCores;
    private float tier;
    private boolean dispatchable;

    public DispatchJob() {
        tier = 0.0f;
    }

    public float getTier() {
        return tier;
    }

    @Override
    public boolean isDispatchable() {
        // TODO Auto-generated method stub
        if (runCores >= maxCores) {
            return false;
        }
        return dispatchable;
    }

    public void setMinCores(int cores) {
        this.minCores = cores;
        recalculate();
    }

    public void setMaxCores(int cores) {
        this.minCores = cores;
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
}
