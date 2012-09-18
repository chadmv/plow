package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.test.AbstractTest;

public class QuotaDaoTests extends AbstractTest {

    @Resource
    ClusterDao clusterDao;

    @Resource
    QuotaDao quotaDao;

    @Test
    public void testCreate() {
        Cluster c = clusterDao.create("test", "test");
        Quota q1 = quotaDao.create(testProject, c, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
    }

    @Test
    public void testGet() {
        Cluster c = clusterDao.create("test", "test");
        Quota q1 = quotaDao.create(testProject, c, 10, 100);
        Quota q2 = quotaDao.get(q1.getQuotaId());
        Quota q3 = quotaDao.get(q1.getQuotaId());
        assertEquals(q1, q2);
        assertEquals(q2, q3);
    }
}
