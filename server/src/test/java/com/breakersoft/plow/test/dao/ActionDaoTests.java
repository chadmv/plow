package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.dao.ActionDao;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ActionType;

public class ActionDaoTests extends AbstractTest {

	@Resource
	private FilterDao filterDao;

	@Resource
	private ActionDao actionDao;

	private Filter filter;

	@Before
	public void init() {
		filter = filterDao.create(TEST_PROJECT, "test");
	}

	@Test
	public void testCreate() {
		Action a1 = actionDao.create(filter, ActionType.PAUSE, "True");
		Action a2 = actionDao.getAction(a1.getActionId());
		assertEquals(a1, a2);
		assertEquals(a2.getFilterId(), filter.getFilterId());
	}
}
