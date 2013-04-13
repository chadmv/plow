package com.breakersoft.plow;

import java.util.UUID;

public class LayerE implements Layer {

    private UUID layerId;
    private UUID jobId;
    private String name;

    public LayerE() { }

    public LayerE(Task task) {
        layerId = task.getLayerId();
        jobId = task.getJobId();
    }

    public UUID getLayerId() {
        return layerId;
    }
    public void setLayerId(UUID layerId) {
        this.layerId = layerId;
    }
    public UUID getJobId() {
        return jobId;
    }
    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int hashCode() {
        return layerId.hashCode();
    }

    public String toString() {
        return String.format("%s [%s]", name, layerId);
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Layer other = (Layer) obj;
        return layerId.equals(other.getLayerId());
    }
}
