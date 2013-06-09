package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;

public interface ClusterDao {

    Cluster get(UUID id);

    Cluster get(String name);

    void setDefault(Cluster cluster);

    Cluster getDefault();

    Cluster create(String name, String[] tags);

    boolean delete(Cluster cluster);

    boolean setLocked(Cluster cluster, boolean value);

    void setName(Cluster cluster, String name);

    void setTags(Cluster cluster, String[] tags);
}
