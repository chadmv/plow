package com.breakersoft.plow;

import java.util.UUID;

public class LayerE implements Layer {

	private UUID layerId;
	private UUID jobId;

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
    public int hashCode() {
    	return layerId.hashCode();
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
