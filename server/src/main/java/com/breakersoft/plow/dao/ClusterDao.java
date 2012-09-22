package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;

public interface ClusterDao {

    Cluster create(String name, String tag);

    Cluster getCluster(UUID id);

    Cluster getCluster(String name);

}
