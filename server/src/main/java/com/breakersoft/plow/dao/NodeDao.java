package com.breakersoft.plow.dao;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.thrift.NodeState;
import com.breakersoft.plow.thrift.SlotMode;

public interface NodeDao {

    Node create(Cluster cluster, Ping ping);

    Node get(String hostname);

    Node get(UUID id);

    void allocate(Node node, int cores, int memory);

    void free(Node node, int cores, int memory);

    void update(Node node, Ping ping);

    void setLocked(Node node, boolean locked);

    void setCluster(Node node, Cluster cluster);

    boolean hasProcs(Node node, boolean withLock);

    void setTags(Node node, Set<String> tags);

    boolean setState(Node node, NodeState state);

    List<Node> getUnresponsiveNodes();

    void setSlotMode(Node node, SlotMode mode, int cores, int ram);
}
