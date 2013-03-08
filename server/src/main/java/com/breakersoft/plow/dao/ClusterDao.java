package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;

public interface ClusterDao {

    Cluster getCluster(UUID id);

    Cluster getCluster(String name);

    void setDefaultCluster(Cluster cluster);

    Cluster getDefaultCluster();

	Cluster create(String name, String[] tags);

	boolean delete(Cluster c);
}
