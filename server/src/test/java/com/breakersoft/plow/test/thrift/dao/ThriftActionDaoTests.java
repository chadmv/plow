package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.service.FilterService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ActionT;
import com.breakersoft.plow.thrift.ActionType;
import com.breakersoft.plow.thrift.dao.ThriftActionDao;

public class ThriftActionDaoTests extends AbstractTest {

	@Resource
	private FilterService filterService;

	@Resource
	private ThriftActionDao thriftActionDao;

	private Filter filter;

	private Action action;


	@Before
	public void init() {
		filter = filterService.createFilter(TEST_PROJECT, "test");
		action = filterService.createAction(filter, ActionType.STOP_PROCESSING, null);
	}

	@Test
	public void testGetAction() {
		ActionT taction = thriftActionDao.get(action.getActionId());
		assertEquals(action.getActionId().toString(), taction.id);
		assertNull(taction.value);
	}

	@Test
	public void testGetAllActions() {
		filterService.createAction(filter, ActionType.PAUSE, "true");
		filterService.createAction(filter, ActionType.SET_FOLDER, UUID.randomUUID().toString());
		filterService.createAction(filter, ActionType.SET_MAX_CORES, "1");
		filterService.createAction(filter, ActionType.SET_MIN_CORES, "1");
		assertEquals(5, thriftActionDao.getAll(filter).size());
	}
}
