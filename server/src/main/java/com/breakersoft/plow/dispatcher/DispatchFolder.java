package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FolderE;

public final class DispatchFolder extends FolderE implements Dispatchable  {

    private int minCores;
    private int maxCores;
    private int runCores;

    private float tier;
    private boolean dispatchable;

    public DispatchFolder(Folder folder) {
        this();
        setFolderId(folder.getFolderId());
        setProjectId(folder.getProjectId());
    }

    public DispatchFolder() {
        tier = 0.0f;
    }

    public int getMaxCores() {
        return maxCores;
    }

    public void setMaxCores(int maxCores) {
        this.maxCores = maxCores;
    }

    public int getMinCores() {
        return maxCores;
    }

    public void setMinCores(int maxCores) {
        this.maxCores = maxCores;
    }

    public int getRunCores() {
        return maxCores;
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
        runCores = runCores + inc;
    }

    @Override
    public void decrementCores(int dec) {
        runCores = runCores - dec;
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
