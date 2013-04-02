package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.service.FilterService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherT;
import com.breakersoft.plow.thrift.MatcherType;
import com.breakersoft.plow.thrift.dao.ThriftMatcherDao;

public class ThriftMatcherDaoTests extends AbstractTest {

	@Resource
	private FilterService filterService;

	@Resource
	private MatcherDao matcherDao;

	@Resource
	private ThriftMatcherDao thriftMatcherDao;

	private Filter filter;
	private Matcher matcher1;
	private Matcher matcher2;

	@Before
	public void init() {
		filter = filterService.createFilter(TEST_PROJECT, "test");
		matcher1 = matcherDao.create(filter, MatcherField.JOB_NAME, MatcherType.CONTAINS, "foo");
		matcher2 = matcherDao.create(filter, MatcherField.USER, MatcherType.CONTAINS, "baggins");
	}

	@Test
	public void testGet() {
		MatcherT matcher = thriftMatcherDao.get(matcher1.getMatcherId());
		assertEquals(matcher.id, matcher1.getMatcherId().toString());
	}

	@Test
	public void testGetAll() {
		assertEquals(2, thriftMatcherDao.getAll(filter).size());
	}

}
