package com.breakersoft.plow.dao;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeDao {

    Node create(Cluster cluster, Ping ping);

    Node get(String hostname);

}
