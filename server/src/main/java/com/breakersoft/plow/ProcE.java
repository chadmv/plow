package com.breakersoft.plow;

import java.util.UUID;

public class ProcE implements Proc {

    private String hostname;
    private UUID procId;
    private UUID taskId;
    private UUID nodeId;
    private UUID jobId;

    public UUID getProcId() {
        return procId;
    }
    public void setProcId(UUID procId) {
        this.procId = procId;
    }
    public UUID getTaskId() {
        return taskId;
    }
    public void setTaskId(UUID frameId) {
        this.taskId = frameId;
    }
    public UUID getNodeId() {
        return nodeId;
    }
    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int hashCode() {
        return procId.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;

        try {
            Proc other = (Proc) obj;
            return procId.equals(other.getProcId());
        } catch (ClassCastException e) {
            return false;
        }
    }
    public UUID getJobId() {
        return jobId;
    }
    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}
