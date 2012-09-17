package com.breakersoft.plow;

import java.util.UUID;

public class ProcE implements Proc {

    private UUID procId;
    private UUID quotaId;
    private UUID frameId;
    private UUID nodeId;

    public UUID getProcId() {
        return procId;
    }
    public void setProcId(UUID procId) {
        this.procId = procId;
    }
    public UUID getQuotaId() {
        return quotaId;
    }
    public void setQuotaId(UUID quotaId) {
        this.quotaId = quotaId;
    }
    public UUID getTaskId() {
        return frameId;
    }
    public void setFrameId(UUID frameId) {
        this.frameId = frameId;
    }
    public UUID getNodeId() {
        return nodeId;
    }
    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }
}
