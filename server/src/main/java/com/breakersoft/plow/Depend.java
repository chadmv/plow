package com.breakersoft.plow;

import java.util.UUID;

import com.breakersoft.plow.thrift.DependType;

public interface Depend {

    UUID getDependId();
    DependType getType();

    UUID getDependentJobId();
    UUID getDependOnJobId();
    UUID getDependentLayerId();
    UUID getDependOnLayerId();
    UUID getDependentTaskId();
    UUID getDependOnTaskId();
}
