package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Before;
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

public class QuotaDaoTests extends AbstractTest {

    @Resource
    QuotaDao quotaDao;

    @Resource
    JobService jobService;

    @Resource
    NodeService nodeService;

    private Cluster cluster;

    @Before
    public void init() {
        cluster = nodeService.createCluster("test");
    }

    @Test
    public void testCreate() {
        Quota q1 = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
    }

    @Test
    public void testCheck() {
        Quota q = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
        assertTrue(quotaDao.check(cluster, TEST_PROJECT));
        simpleJdbcTemplate.update("UPDATE quota SET int_cores_run=100 WHERE pk_quota=?", q.getQuotaId());
        assertFalse(quotaDao.check(cluster, TEST_PROJECT));
    }

    @Test
    public void testSetSize() {
        Quota quota = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
        quotaDao.setSize(quota, 1);
        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT int_size FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void testSetBurst() {
        Quota quota = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
        quotaDao.setBurst(quota, 1);
        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT int_burst FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void testSetLocked() {
        Quota quota = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
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
        Quota q1 = quotaDao.create(TEST_PROJECT, cluster, 10, 100);
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
        Quota quota = quotaDao.create(TEST_PROJECT, cluster, 10, 100);

        quotaDao.allocate(cluster, TEST_PROJECT, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void freeResources() {
        Quota quota = quotaDao.create(TEST_PROJECT, cluster, 10, 100);

        quotaDao.allocate(cluster, TEST_PROJECT, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.free(quota, 1);
        assertEquals(4,
                simpleJdbcTemplate.queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.allocate(cluster, TEST_PROJECT, 6);
        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.free(quota, 10);
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT int_cores_run FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }
}
