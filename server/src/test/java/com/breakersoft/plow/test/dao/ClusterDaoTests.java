package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.test.AbstractTest;

public class ClusterDaoTests extends AbstractTest {

    @Resource
    ClusterDao clusterDao;

    @Test
    public void create() {
        clusterDao.create("default", "default");
    }

    @Test
    public void getByUUID() {
        Cluster c1 = clusterDao.create("default", "default");
        Cluster c2 = clusterDao.getCluster(c1.getClusterId());
        assertEquals(c1, c2);
    }

    @Test
    public void getByName() {
        Cluster c1 = clusterDao.create("default", "default");
        Cluster c2 = clusterDao.getCluster("default");
        assertEquals(c1, c2);
    }
}
