package com.breakersoft.plow;

import java.util.UUID;

public interface Job extends Project, JobId {
    public UUID getJobId();
    public UUID getProjectId();
    public UUID getFolderId();
    public String getName();
}
