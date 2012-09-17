package com.breakersoft.plow;

import java.util.UUID;

public class ClusterE implements Cluster {

    private UUID clusterId;

    @Override
    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public int hashCode() {
        return clusterId.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Cluster other = (Cluster) obj;
        return clusterId.equals(other.getClusterId());
    }
}
