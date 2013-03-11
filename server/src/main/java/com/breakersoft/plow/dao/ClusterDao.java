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

	boolean setClusterLocked(Cluster c, boolean value);

	void setClusterName(Cluster c, String name);

	void setClusterTags(Cluster c, String[] tags);
}
