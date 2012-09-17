package com.breakersoft.plow;

import java.util.UUID;

public interface Proc {

    UUID getProcId();
    UUID getTaskId();
    UUID getQuotaId();
    UUID getNodeId();

}
