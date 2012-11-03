package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

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

public class QuotaDaoTests extends AbstractTest {

    @Resource
    QuotaDao quotaDao;

    @Resource
    JobService jobService;

    @Resource
    NodeService nodeService;

    @Test
    public void testCreate() {
        Cluster c = nodeService.createCluster("test", "test");
        Quota q1 = quotaDao.create(TEST_PROJECT, c, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
    }

    @Test
    public void testGet() {
        Cluster c = nodeService.createCluster("test", "test");
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
        Cluster c = nodeService.createCluster("test", "test");
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);

        quotaDao.allocateResources(quota, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }

    @Test
    public void freeResources() {
        Cluster c = nodeService.createCluster("test", "test");
        Quota quota = quotaDao.create(TEST_PROJECT, c, 10, 100);

        quotaDao.allocateResources(quota, 5);
        assertEquals(5,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.freeResources(quota, 1);
        assertEquals(4,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.allocateResources(quota, 6);
        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));

        quotaDao.freeResources(quota, 10);
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT int_run_cores FROM quota WHERE pk_quota=?",
                        quota.getQuotaId()));
    }
}
