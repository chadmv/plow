package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ProjectT;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;

public class ThriftProjectDaoTests extends AbstractTest {

    @Autowired
    ThriftProjectDao thriftProjectDao;

    @Test
    public void testGetByCode() {
        ProjectT project = thriftProjectDao.get("unittest");
        assertEquals("unittest", project.code);
    }

    @Test
    public void testGetById() {
        ProjectT project_a = thriftProjectDao.get("unittest");
        ProjectT project_b = thriftProjectDao.get(UUID.fromString(project_a.id));
        assertEquals(project_a.id, project_b.id);
    }

    @Test
    public void testGetAll() {
    	thriftProjectDao.all();
    }

    @Test
    public void testGetActive() {
    	thriftProjectDao.active();
    }


    @Test
    public void getPlowtime() {
        thriftProjectDao.getPlowTime();
    }
}
