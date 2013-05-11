package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Sets;

public class NodeDaoTests extends AbstractTest {

    @Resource
    NodeDao nodeDao;

    @Resource
    ClusterDao clusterDao;

    private static final String[] TAGS = new String[] { "test" } ;

    @Test
    public void create() {
        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, ping);
        assertEquals(cluster.getClusterId(), node.getClusterId());
        assertEquals(ping.getHostname(), node.getName());
    }

    @Test
    public void lock() {
        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, ping);
        nodeDao.setLocked(node, true);

        boolean locked = jdbc().queryForObject(
                "SELECT bool_locked FROM plow.node WHERE pk_node=?", Boolean.class, node.getNodeId());
        assertEquals(true, locked);
    }

    @Test
    public void setCluster() {
        Ping ping = getTestNodePing();
        Cluster cluster1 = clusterDao.create("test1", TAGS);
        Cluster cluster2 = clusterDao.create("test2", TAGS);
        Node node = nodeDao.create(cluster1, ping);
        nodeDao.setCluster(node, cluster2);

        Node copy = nodeDao.get(node.getNodeId());
        assertEquals(cluster2.getClusterId(), copy.getClusterId());
    }

    @Test
    public void setTags() {
        Ping ping = getTestNodePing();
        Cluster cluster1 = clusterDao.create("test1", TAGS);
        Node node = nodeDao.create(cluster1, ping);
        nodeDao.setTags(node, Sets.newHashSet("a1", "b2", "c3"));
        List<String> tags = jdbc().query(
                "SELECT unnest(str_tags) FROM plow.node WHERE pk_node=?", JdbcUtils.STRING_MAPPER, node.getNodeId());
        assertEquals(3, tags.size());
        assertTrue(tags.contains("a1"));
        assertTrue(tags.contains("b2"));
        assertTrue(tags.contains("c3"));
    }

    @Test
    public void update() {
        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, ping);
        nodeDao.update(node, ping);

        int swap = simpleJdbcTemplate.queryForInt("SELECT int_swap FROM node_sys WHERE pk_node=?",
                node.getNodeId());
        assertEquals(ping.hw.totalSwapMb, swap);
    }

    @Test
    public void allocateResources() {
        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, ping);
        nodeDao.allocate(node, 1, 1024);

        // Check to ensure the procs/memory were subtracted from the host.
        assertEquals(ping.hw.physicalCpus - 1,
                simpleJdbcTemplate.queryForInt("SELECT int_idle_cores FROM node_dsp WHERE pk_node=?",
                        node.getNodeId()));

        assertEquals(ping.hw.totalRamMb - 1024 - Defaults.MEMORY_RESERVE_MB,
                simpleJdbcTemplate.queryForInt("SELECT int_free_ram FROM node_dsp WHERE pk_node=?",
                        node.getNodeId()));

    }

    public void allocateResourcesFailed() {
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, getTestNodePing());
        nodeDao.allocate(node, 100, 1000000);
    }

    @Test
    public void freeResources() {

        Ping ping = getTestNodePing();
        Cluster cluster = clusterDao.create("test", TAGS);
        Node node = nodeDao.create(cluster, ping);
        nodeDao.allocate(node, 1, 1024);
        nodeDao.free(node, 1, 1024);

        assertEquals(ping.hw.physicalCpus,
                simpleJdbcTemplate.queryForInt("SELECT int_idle_cores FROM node_dsp WHERE pk_node=?",
                        node.getNodeId()));

        assertEquals(ping.hw.totalRamMb - Defaults.MEMORY_RESERVE_MB,
                simpleJdbcTemplate.queryForInt("SELECT int_free_ram FROM node_dsp WHERE pk_node=?",
                        node.getNodeId()));
    }
}
