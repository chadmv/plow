package com.breakersoft.plow.test.dao;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.test.AbstractTest;
import com.google.common.collect.Lists;

public class NodeDaoTest extends AbstractTest {

    @Resource
    NodeDao nodeDao;;

    @Resource
    ClusterDao clusterDao;

    @Test
    public void create() {

        Cluster cluster = clusterDao.create("test", "test",
                Lists.newArrayList("test2"));
        Node node = nodeDao.create(cluster, getTestNodePing());


    }
}
