package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.test.AbstractTest;

public class ClusterDaoTests extends AbstractTest {

    @Resource
    ClusterDao clusterDao;

    @Resource
    NodeDao nodeDao;

    private final static String[] TAGS = new String[] {"test"};

    @Test
    public void create() {
        clusterDao.create("default", TAGS);
    }

    @Test
    public void delete() {
        Cluster c = clusterDao.create("default", TAGS);
        assertTrue(clusterDao.delete(c));
        assertFalse(clusterDao.delete(c));
    }

    @Test(expected=DataIntegrityViolationException.class)
    public void deleteWithNodeAssigned() {
        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        nodeDao.create(cluster, ping);
        clusterDao.delete(cluster);
    }

    @Test
    public void getByUUID() {
        Cluster c1 = clusterDao.create("default", TAGS);
        Cluster c2 = clusterDao.getCluster(c1.getClusterId());
        assertEquals(c1, c2);
    }

    @Test
    public void getByName() {
        Cluster c1 = clusterDao.create("default", TAGS);
        Cluster c2 = clusterDao.getCluster("default");
        assertEquals(c1, c2);
    }
}
