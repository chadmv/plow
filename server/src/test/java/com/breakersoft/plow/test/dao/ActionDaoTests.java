package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.ActionFull;
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
    public void testCreateAndGet() {
        Action a1 = actionDao.create(filter, ActionType.PAUSE, "True");
        Action a2 = actionDao.get(a1.getActionId());
        assertEquals(a1, a2);
        assertEquals(a2.getFilterId(), filter.getFilterId());
    }

    @Test(expected=EmptyResultDataAccessException.class)
    public void testDelete() {
        Action a1 = actionDao.create(filter, ActionType.PAUSE, "True");
        actionDao.delete(a1);
        actionDao.get(a1.getActionId());
    }

    @Test
    public void testGetFullAll() {
         Action a1 = actionDao.create(filter, ActionType.PAUSE, "True");
         List<ActionFull> actions = actionDao.getAllFull(filter);
         assertEquals(1, actions.size());
         assertEquals(a1.getActionId(), actions.get(0).getActionId());
    }

    @Test
    public void testGetFull() {
         Action a1 = actionDao.create(filter, ActionType.PAUSE, "True");
         ActionFull action = actionDao.getFull(a1);
         assertEquals(a1.getActionId(), action.getActionId());
         assertEquals(ActionType.PAUSE, action.type);
         assertEquals("True", action.value);
    }
}
