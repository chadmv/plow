package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.NodeFilterT;
import com.breakersoft.plow.thrift.NodeT;

public interface ThriftNodeDao {

    List<NodeT> getNodes(NodeFilterT filter);

    NodeT getNode(String id);

    NodeT getNode(UUID id);
}
