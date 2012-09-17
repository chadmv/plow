package com.breakersoft.plow;

import java.util.UUID;

public class TaskE implements Task {

	private UUID layerId;
	private UUID frameId;
	
	@Override
	public UUID getLayerId() {
		return layerId;
	}

	@Override
	public UUID getTaskId() {
		return frameId;
	}
	
	public void setLayerId(UUID layerId) {
		this.layerId = layerId;
	}
	
	public void setTaskId(UUID frameId) {
		this.frameId = frameId;
	}
	
    public int hashCode() {
    	return frameId.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Task other = (Task) obj;
        return frameId.equals(other.getTaskId());
    }
}
