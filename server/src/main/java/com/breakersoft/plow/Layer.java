package com.breakersoft.plow;

import java.util.UUID;

public interface Layer {

    public UUID getLayerId();
    public UUID getJobId();
    public String getName();

}
