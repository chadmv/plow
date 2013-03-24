package com.breakersoft.plow.dao;

import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeDao {

    Node create(Cluster cluster, Ping ping);

    Node get(String hostname);

    Node get(UUID id);

    boolean allocateResources(Node node, int cores, int memory);

    void freeResources(Node node, int cores, int memory);

	void update(Node node, Ping ping);

	void setLocked(Node node, boolean locked);

	void setCluster(Node node, Cluster cluster);

	boolean hasProcs(Node node);

	void setTags(Node node, Set<String> tags);

}
