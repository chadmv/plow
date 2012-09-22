package com.breakersoft.plow;

import java.util.UUID;

public class NodeE implements Node {

    private UUID nodeId;
    private UUID clusterId;
    private String name;

    @Override
    public UUID getNodeId() {
        return nodeId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setNodeId(UUID nodeId) {
        this.nodeId = nodeId;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID id) {
        this.clusterId = id;
    }
}
