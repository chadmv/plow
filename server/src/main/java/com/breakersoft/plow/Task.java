package com.breakersoft.plow;

import java.util.UUID;

public interface Task {

    public UUID getJobId();
    public UUID getLayerId();
    public UUID getTaskId();

}
