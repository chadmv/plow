package com.breakersoft.plow.thrift.dao;

import java.util.List;

import com.breakersoft.plow.thrift.ClusterT;

public interface ThriftClusterDao {

	ClusterT getCluster(String id);

	List<ClusterT> getClusters();

	List<ClusterT> getClusters(String tag);
}
