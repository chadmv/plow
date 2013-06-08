package com.breakersoft.plow;

import java.util.UUID;

public class ClusterE implements Cluster {

    private UUID clusterId;
    private String name;

    @Override
    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
