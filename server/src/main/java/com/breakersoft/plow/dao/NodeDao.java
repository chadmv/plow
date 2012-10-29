package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeDao {

    Node create(Cluster cluster, Ping ping);

    Node get(String hostname);

    Node get(UUID id);

    void allocateResources(Node node, int cores, int memory);

    void freeResources(Node node, int cores, int memory);

}
