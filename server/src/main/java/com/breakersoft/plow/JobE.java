package com.breakersoft.plow;

import java.util.UUID;

public class JobE implements Job {

    private UUID jobId;
    private UUID folderId;
    private UUID projectId;
    private String name;

    public JobE() { }

    public JobE(UUID id) {
        this.jobId = id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID id) {
        this.jobId = id;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public int hashCode() {
        return jobId.hashCode();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;

        Job other = (Job) obj;
        return jobId.equals(other.getJobId());
    }
}
