package com.breakersoft.plow;

import java.util.UUID;

public class ProjectE implements Project {

    private UUID projectId;
    private String code;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int hashCode() {
        return projectId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Project: %s [%s]", code, projectId);
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
