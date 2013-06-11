package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.QuotaFilterT;
import com.breakersoft.plow.thrift.QuotaT;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;
import com.breakersoft.plow.thrift.dao.ThriftQuotaDao;

public class ThriftQuotaDaoTests extends AbstractTest {

	@Autowired
	ThriftQuotaDao thriftQuotaDao;

	@Autowired
	ThriftProjectDao thriftProjectDao;

	@Autowired
	ThriftClusterDao thriftClusterDao;

	@Test
	public void testGetQuotas() {
		QuotaFilterT filter = new QuotaFilterT();
		List<QuotaT> quotas = thriftQuotaDao.getQuotas(filter);
		assertTrue(quotas.size() > 0);

		// Try to get unittest.unittest quota
		filter.addToCluster(TEST_CLUSTER.getClusterId().toString());
		quotas = thriftQuotaDao.getQuotas(filter);
		assertTrue(quotas.size() == 1);
		assertEquals("unittest.unittest", quotas.get(0).name);

		// Try to get unittest.unittest quota
		filter.addToProject(TEST_PROJECT.getProjectId().toString());
		quotas = thriftQuotaDao.getQuotas(filter);
		assertTrue(quotas.size() == 1);
		assertEquals("unittest.unittest", quotas.get(0).name);

		// Make a query that returns nothing
		filter = new QuotaFilterT();
		filter.addToCluster("69062903-AE3E-464C-B790-A621F06424B4");
		quotas = thriftQuotaDao.getQuotas(filter);
		assertTrue(quotas.size() == 0);
	}

	@Test
	public void testGetQuotaById() {

		QuotaFilterT filter = new QuotaFilterT();
		List<QuotaT> quotas = thriftQuotaDao.getQuotas(filter);

		QuotaT q = thriftQuotaDao.getQuota(UUID.fromString(quotas.get(0).id));
		assertEquals(quotas.get(0), q);
	}

}
