package com.breakersoft.plow.service;

import com.breakersoft.plow.Node;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeService {

    boolean nodeExists(String hostname);
    Node createNode(Ping ping);
    Node getNode(String hostname);
    void updateNode(Node node, Ping ping);

}
