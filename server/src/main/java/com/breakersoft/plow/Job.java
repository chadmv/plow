package com.breakersoft.plow;

import java.util.UUID;

public interface Job {
    public UUID getJobId();
    public UUID getProjectId();
    public UUID getFolderId();
    public String getName();
}
