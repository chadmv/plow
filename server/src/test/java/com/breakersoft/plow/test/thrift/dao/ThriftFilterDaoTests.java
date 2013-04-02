package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.FilterT;
import com.breakersoft.plow.thrift.dao.ThriftFilterDao;

public class ThriftFilterDaoTests extends AbstractTest {

	@Resource
	private ThriftFilterDao thriftFilterDao;

	@Resource
	private FilterDao filterDao;

	private Filter filter1;
	private Filter filter2;

	@Before
	public void testCreate() {
		filter1 = filterDao.create(TEST_PROJECT, "test1");
		filter2 = filterDao.create(TEST_PROJECT, "test2");
	}

	@Test
	public void testGetAll() {
		assertEquals(2, thriftFilterDao.getAll(TEST_PROJECT).size());
	}

	@Test
	public void testGet() {
		FilterT ft = thriftFilterDao.get(filter1.getFilterId());
		assertEquals(ft.id, filter1.getFilterId().toString());
	}
}
