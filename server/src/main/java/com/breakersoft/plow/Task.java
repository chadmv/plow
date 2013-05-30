package com.breakersoft.plow;

import java.util.UUID;

public interface Task extends JobId {

    public UUID getJobId();
    public UUID getLayerId();
    public UUID getTaskId();
    public String getName();

}
