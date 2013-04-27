package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;
import com.google.common.collect.Sets;

public class QuotaDaoTests extends AbstractTest {

    @Resource
    QuotaDao quotaDao;

    @Resource
    JobService jobService;

    @Resource
    NodeService nodeService;

    private final static Set<String> TAGS = Sets.newHashSet("test");

    @Test
    public void testCreate() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota q1 = quotaDao.create(TEST_PROJECT, c, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
    }

    @Test
    public void testCheck() {
        Cluster c = nodeService.createCluster("test", TAGS);
        quotaDao.create(TEST_PROJECT, c, 10, 100);
        assertTrue(quotaDao.check(c, TEST_PROJECT, 99));
        assertFalse(quotaDao.check(c, TEST_PROJECT, 100));
        assertFalse(quotaDao.check(c, TEST_PROJECT, 101));
        assertTrue(quotaDao.check(c, TEST_PROJECT, 0));
    }

    @Test
    public void testSetSize() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);
        quotaDao.setSize(quota, 1);
        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT int_size FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void testSetBurst() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);
        quotaDao.setBurst(quota, 1);
        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT int_burst FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void testSetLocked() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);
        quotaDao.setLocked(quota, true);
        assertEquals(true,
                simpleJdbcTemplate.queryForObject("SELECT bool_locked FROM quota WHERE pk_quota=?",
                        Boolean.class,
                        quota.getQuotaId()));

        quotaDao.setLocked(quota, false);
        assertEquals(false,
                simpleJdbcTemplate.queryForObject("SELECT bool_locked FROM quota WHERE pk_quota=?",
                        Boolean.class,
                        quota.getQuotaId()));
    }

    @Test
    public void testGet() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota q1 = quotaDao.create(TEST_PROJECT, c, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        Quota q3 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
        assertEquals(q2, q3);
    }

    @Test
    public void testGetByNodeAndTask() {
        Node node = nodeService.createNode(getTestNodePing());
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        Layer layer = jobService.getLayer(event.getJob(),
                event.getJobSpec().getLayers().get(0).name);
        Task task = jobService.getTask(layer, 1);
        Quota quota = quotaDao.getQuota(node, task);

        assertEquals(node.getClusterId(), quota.getClusterId());
        assertEquals(event.getJob().getProjectId(), quota.getProjectId());
    }

    @Test
    public void allocateResources() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);

        quotaDao.allocate(c, TEST_PROJECT, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void freeResources() {
        Cluster c = nodeService.createCluster("test", TAGS);
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);

        quotaDao.allocate(c, TEST_PROJECT, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.free(quota, 1);
        assertEquals(4,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.allocate(c, TEST_PROJECT, 6);
        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.free(quota, 10);
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }
}
