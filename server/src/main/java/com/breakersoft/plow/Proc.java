package com.breakersoft.plow;

import java.util.UUID;

public interface Proc {

    String getHostname();
    UUID getProcId();
    UUID getTaskId();
    UUID getNodeId();
    UUID getJobId();

}
