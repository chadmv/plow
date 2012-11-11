package com.breakersoft.plow.thrift.dao;

import java.util.List;

import com.breakersoft.plow.thrift.NodeFilterT;
import com.breakersoft.plow.thrift.NodeT;

public interface ThriftNodeDao {

    List<NodeT> getNodes(NodeFilterT filter);

}
