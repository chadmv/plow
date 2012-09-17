package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.JobE;

public final class DispatchJob extends JobE implements Dispatchable {

    private int minCores;
    private int maxCores;
    private int cores;
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
        if (cores >= maxCores) {
            return false;
        }
        return dispatchable;
    }

    @Override
    public void incrementCores(int inc) {
        cores = cores + inc;
    }

    @Override
    public void decrementCores(int dec) {
        cores = cores - dec;
    }

    @Override
    public void recalculate() {
        if (minCores <= 0) {
            tier = cores;
        }
        else {
            tier = cores / minCores;
        }
    }
}
