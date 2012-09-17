package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Cluster;

public interface ClusterDao {

    Cluster create(String name, String tag, List<String> tags);

    Cluster getCluster(UUID id);

    Cluster getCluster(String id);

}
