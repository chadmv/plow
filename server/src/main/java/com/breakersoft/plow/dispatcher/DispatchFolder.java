package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.FolderE;

public final class DispatchFolder extends FolderE implements Dispatchable  {

    private int minCores;
    private int maxCores;
    private int cores;

    private float tier;
    private boolean dispatchable;

    public DispatchFolder() {
        tier = 0.0f;
    }

    @Override
    public float getTier() {
        // TODO Auto-generated method stub
        return tier;
    }

    @Override
    public boolean isDispatchable() {
        // TODO Auto-generated method stub
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
