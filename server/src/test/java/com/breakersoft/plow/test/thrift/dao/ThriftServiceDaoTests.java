package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.dao.ServiceDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ServiceT;
import com.breakersoft.plow.thrift.dao.ThriftServiceDao;

public class ThriftServiceDaoTests extends AbstractTest {

    @Resource
    ThriftServiceDao thriftServiceDao;

    @Resource
    ServiceDao serviceDao;

    @Test
    public void testGetByName() {

        ServiceT newService = new ServiceT();
        newService.name = "foo";
        serviceDao.create(newService);

        ServiceT service = thriftServiceDao.getService("foo");
        assertEquals(newService, service);
    }

    @Test
    public void testGetById() {

        ServiceT newService = new ServiceT();
        newService.name = "foo";
        newService.setMaxCores(10);
        newService.setMinCores(1);
        Service svc = serviceDao.create(newService);

        ServiceT service = thriftServiceDao.getService(svc.getServiceId());
        assertEquals(newService, service);
    }

    @Test
    public void getServices() {

        ServiceT newService = new ServiceT();
        newService.name = "foo";
        serviceDao.create(newService);

        assertTrue(thriftServiceDao.getServices().size() > 0);
    }
}
