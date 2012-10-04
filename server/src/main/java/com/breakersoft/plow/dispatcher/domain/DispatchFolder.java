package com.breakersoft.plow.dispatcher.domain;

import com.breakersoft.plow.Folder;
import com.breakersoft.plow.FolderE;
import com.breakersoft.plow.dispatcher.Dispatchable;

public final class DispatchFolder extends FolderE implements Dispatchable  {

    private int minCores;
    private int maxCores;
    private int runCores;
    private float tier;

    public DispatchFolder() {
        tier = 0.0f;
    }

    public DispatchFolder(Folder folder) {
        this();
        setFolderId(folder.getFolderId());
        setProjectId(folder.getProjectId());
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
        recalculate();
    }

    public int getRunCores() {
        return maxCores;
    }

    @Override
    public float getTier() {
        return tier;
    }

    @Override
    public boolean isDispatchable() {
        return true;
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
