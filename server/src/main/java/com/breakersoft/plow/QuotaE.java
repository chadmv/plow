package com.breakersoft.plow;

import java.util.UUID;

public class QuotaE implements Quota {

    private UUID quotaId;
    private UUID clusterId;
    private UUID projectId;

    public void setQuotaId(UUID quotaId) {
        this.quotaId = quotaId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    @Override
    public UUID getClusterId() {
        return clusterId;
    }

    @Override
    public UUID getQuotaId() {
        return quotaId;
    }

    public int hashCode() {
        return quotaId.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Quota other = (Quota) obj;
        return quotaId.equals(other.getQuotaId());
    }

}
