package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.Job;
import com.google.common.collect.ComparisonChain;

public class DispatchableJob implements Job, Comparable<DispatchableJob> {

    public DispatchableFolder folder;

    public UUID jobId;
    public UUID folderId;
    public UUID projectId;

    public int minCores;
    public int maxCores;
    public volatile int runCores;
    public boolean isDispatchable = true;
    public float tier = 0.0f;
    public Set<String> tags;

    @Override
    public int compareTo(DispatchableJob other) {
        return ComparisonChain.start()
                .compare(folder.tier, other.folder.tier)
                .compare(tier, other.tier)
                .result();
    }

    public synchronized int incrementAndGetCores(int delta) {
        runCores = runCores + delta;
        if (minCores == 0) {
            tier = runCores;
        }
        else {
            tier = runCores / (float) minCores;
        }
        return runCores;
    }

    public boolean isDispatchable() {
        if (runCores >= maxCores && maxCores > -1) {
            return false;
        }

        return isDispatchable;
    }

    @Override
    public UUID getJobId() {
        return jobId;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public UUID getFolderId() {
        return folderId;
    }

    public int hashCode() {
        return jobId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Job other = (Job) obj;
        return jobId.equals(other.getJobId());
    }
}
