package com.breakersoft.plow;

import java.util.UUID;

public interface Node extends Cluster {

    UUID getNodeId();
    String getName();


}
