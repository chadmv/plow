package com.breakersoft.plow;

import java.util.UUID;

public class ProjectE implements Project {
	
	private UUID projectId;

	public UUID getProjectId() {
		return projectId;
	}

	public void setProjectId(UUID projectId) {
		this.projectId = projectId;
	}
	
    public int hashCode() {
    	return projectId.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Project other = (Project) obj;
        return projectId.equals(other.getProjectId());
    }
}
