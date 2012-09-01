package com.breakersoft.plow;

import java.util.UUID;

public class FrameE implements Frame {

	private UUID layerId;
	private UUID frameId;
	
	@Override
	public UUID getLayerId() {
		return layerId;
	}

	@Override
	public UUID getFrameId() {
		return frameId;
	}
	
	public void setLayerId(UUID layerId) {
		this.layerId = layerId;
	}
	
	public void setFrameId(UUID frameId) {
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

        Frame other = (Frame) obj;
        return frameId.equals(other.getFrameId());
    }
}
