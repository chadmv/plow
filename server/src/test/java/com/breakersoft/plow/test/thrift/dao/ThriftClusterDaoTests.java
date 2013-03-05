package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ClusterT;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.google.common.collect.Lists;

public class ThriftClusterDaoTests extends AbstractTest {

    @Resource
    ClusterDao clusterDao;

    @Resource
    ThriftClusterDao thriftClusterDao;

    private int standardClusterCount = 0;

    private List<Cluster> clusters = Lists.newArrayList();

    @Before
    public void create() {

    	standardClusterCount = this.jdbc().queryForInt("SELECT COUNT(1) FROM plow.cluster");

    	clusters.clear();
    	for (int i=0; i<10; i++) {
    		clusters.add(clusterDao.create(
    				String.format("default%d", i), String.format("default%d", i)));
    	}
    }

    @Test
    public void testGetClusters() {
    	assertEquals(10 + standardClusterCount, thriftClusterDao.getClusters().size());
    }

    @Test
    public void testGetClustersByTag() {
    	assertEquals(1, thriftClusterDao.getClusters("default1").size());
    }

    @Test
    public void testGetClustersById() {
    	ClusterT cluster_by_name = thriftClusterDao.getCluster("default1");
    	ClusterT cluster_by_id = thriftClusterDao.getCluster(clusters.get(1).getClusterId().toString());
    	assertEquals(cluster_by_name, cluster_by_id);
    }
}
