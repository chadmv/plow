package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.MatcherFull;
import com.breakersoft.plow.dao.FilterDao;
import com.breakersoft.plow.dao.MatcherDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public class MatcherDaoTests  extends AbstractTest {

    @Resource
    private FilterDao filterDao;

    @Resource
    private MatcherDao matcherDao;

    @Test
    public void testCreate() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        assertEquals(f1.getFilterId(), m1.getFilterId());
    }

    @Test
    public void testDelete() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        matcherDao.delete(m1);
        assertEquals(0,
                jdbc().queryForInt("SELECT COUNT(1) FROM matcher WHERE pk_matcher=?", m1.getMatcherId()));
    }

    @Test
    public void testGet() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        Matcher m2 = matcherDao.get(m1.getMatcherId());
        assertEquals(m1, m2);
    }

    @Test
    public void setValue() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        matcherDao.setValue(m1, "xyz.321");

        assertEquals("xyz.321",
                jdbc().queryForObject("SELECT str_value FROM plow.matcher WHERE pk_matcher=?",
                        String.class, m1.getMatcherId()));
    }

    @Test
    public void setType() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        matcherDao.setType(m1, MatcherType.BEGINS_WITH);

        assertEquals(MatcherType.BEGINS_WITH.ordinal(),
                jdbc().queryForInt("SELECT int_type FROM plow.matcher WHERE pk_matcher=?", m1.getMatcherId()));
    }

    @Test
    public void setField() {
        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        Matcher m1 = matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "abc.123");
        matcherDao.setField(m1, MatcherField.USER);

        assertEquals(MatcherField.USER.ordinal(),
                jdbc().queryForInt("SELECT int_field FROM plow.matcher WHERE pk_matcher=?", m1.getMatcherId()));
    }

    @Test
    public void testGetFull() {

        Filter f1 = filterDao.create(TEST_PROJECT, "test");
        matcherDao.create(f1, MatcherField.JOB_NAME, MatcherType.CONTAINS, "test");
        matcherDao.create(f1, MatcherField.USER, MatcherType.IS, "stella");

        List<MatcherFull> matchers = matcherDao.getAllFull(TEST_PROJECT);
        assertEquals(2, matchers.size());
    }
}
