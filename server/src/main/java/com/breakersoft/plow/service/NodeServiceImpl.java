package com.breakersoft.plow.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.rnd.thrift.Ping;

@Service
@Transactional
public class NodeServiceImpl implements NodeService {

    @Autowired
    NodeDao nodeDao;

    @Autowired
    ClusterDao clusterDao;

    @Override
    public boolean nodeExists(String hostname) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node createNode(Ping ping) {
        Cluster cluster = clusterDao.getCluster(
                UUID.fromString("00000000-0000-0000-0000-000000000000"));
        return nodeDao.create(cluster, ping);
    }

    @Override
    public Node getNode(String hostname) {
        return nodeDao.get(hostname);
    }

    @Override
    public void updateNode(Node node, Ping ping) {
        // TODO Auto-generated method stub

    }


}
