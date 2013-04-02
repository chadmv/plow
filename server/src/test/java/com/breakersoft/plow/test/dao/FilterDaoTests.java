package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.util.JdbcUtils;
import com.google.common.collect.Lists;

public class FilterDaoTests extends AbstractTest {

	@Resource
	private FilterDao filterDao;

	@Test
	public void testCreate() {
		Filter f1 = filterDao.create(TEST_PROJECT, "test");
		assertEquals(f1, filterDao.get(f1.getFilterId()));
	}

	@Test
	public void testDelete() {
		Filter f = filterDao.create(TEST_PROJECT, "test");
		assertTrue(filterDao.delete(f));
	}

	@Test
	public void testSetFilterName() {
		Filter f = filterDao.create(TEST_PROJECT, "test");
		filterDao.setName(f, "biblo");
		assertEquals("biblo",
				jdbc().queryForObject("SELECT str_name FROM plow.filter WHERE pk_filter=?", String.class, f.getFilterId()));
	}

	@Test
	public void testReorder() {
		for (int i=0; i<10; i++) {
			filterDao.create(TEST_PROJECT, "test"+i);
		}
		filterDao.reorder(TEST_PROJECT);
		List<Object[]> result = jdbc().query("SELECT int_order::integer from plow.filter WHERE pk_project=? ORDER BY int_order ASC",
				JdbcUtils.OBJECT_ARRAY_MAPPER, TEST_PROJECT.getProjectId());

		int value = 1;
		for (Object[] item: result) {
			assertEquals((Integer) value, (Integer) item[0]);
			value++;
		}
	}

	@Test
	public void testSetOrder() {

		List<Filter> filters = Lists.newArrayList();
		for (int i=0; i<10; i++) {
			Filter f = filterDao.create(TEST_PROJECT, "test"+i);
			filters.add(f);
		}
		filterDao.reorder(TEST_PROJECT);
		Filter middle = filters.get(filters.size() / 2);
		filterDao.setOrder(middle, 0);
		filterDao.reorder(TEST_PROJECT);

		UUID first = jdbc().queryForObject("SELECT pk_filter from plow.filter WHERE pk_project=? ORDER BY int_order ASC LIMIT 1",
				UUID.class, TEST_PROJECT.getProjectId());
		assertEquals(middle.getFilterId(), first);

		filterDao.setOrder(middle, 11);
		filterDao.reorder(TEST_PROJECT);

		UUID last = jdbc().queryForObject("SELECT pk_filter from plow.filter WHERE pk_project=? ORDER BY int_order DESC LIMIT 1",
				UUID.class, TEST_PROJECT.getProjectId());
		assertEquals(middle.getFilterId(), last);
	}

	@Test
	public void testIncreaseOrder() {

		List<Filter> filters = Lists.newArrayList();
		for (int i=0; i<10; i++) {
			Filter f = filterDao.create(TEST_PROJECT, "test"+i);
			filters.add(f);
		}
		filterDao.reorder(TEST_PROJECT);
		Filter middle = filterDao.get(TEST_PROJECT, 5);
		filterDao.increaseOrder(middle);
		filterDao.reorder(TEST_PROJECT);

		int order = jdbc().queryForInt("SELECT int_order FROM plow.filter WHERE pk_filter=?", middle.getFilterId());
		assertEquals(6, order);
	}

	@Test
	public void testDecreaseOrder() {

		List<Filter> filters = Lists.newArrayList();
		for (int i=0; i<10; i++) {
			Filter f = filterDao.create(TEST_PROJECT, "test"+i);
			filters.add(f);
		}
		filterDao.reorder(TEST_PROJECT);
		Filter middle = filterDao.get(TEST_PROJECT, 5);
		filterDao.decreaseOrder(middle);
		filterDao.reorder(TEST_PROJECT);

		int order = jdbc().queryForInt("SELECT int_order::integer FROM plow.filter WHERE pk_filter=?", middle.getFilterId());
		assertEquals(4, order);
	}
}
