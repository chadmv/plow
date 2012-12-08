package com.breakersoft.plow.dispatcher.domain;

import java.util.UUID;

public class DispatchableFolder {

    public UUID folderId;
    public int minCores;
    public int maxCores;
    public int runCores;
    public boolean isDispatchable;
    public float tier;

    public synchronized int incrementAndGetCores(int delta) {
        runCores = runCores + delta;
        if (maxCores == 0) {
            tier = runCores;
        }
        else {
            tier = runCores / (float) maxCores;
        }
        return runCores;
    }
}
