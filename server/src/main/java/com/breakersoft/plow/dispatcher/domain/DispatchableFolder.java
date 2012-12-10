package com.breakersoft.plow.dispatcher.domain;

import java.util.UUID;
import org.slf4j.Logger;

public class DispatchableFolder {

    private static final Logger logger =
            org.slf4j.LoggerFactory.getLogger(DispatchableFolder.class);

    public UUID folderId;
    public int minCores;
    public int maxCores;
    public volatile int runCores;
    public volatile float tier;
    public boolean isDispatchable;

    public synchronized int incrementAndGetCores(int delta) {
        runCores = runCores + delta;
        if (minCores == 0) {
            tier = runCores;
        }
        else {
            tier = runCores / (float) minCores;
        }
        logger.info("Folder:{} is running {} cores", folderId, runCores);
        return runCores;
    }
}
