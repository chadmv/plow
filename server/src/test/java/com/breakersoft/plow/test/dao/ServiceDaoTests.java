package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.postgresql.jdbc4.Jdbc4Array;
import org.springframework.dao.DataIntegrityViolationException;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.dao.ServiceDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ServiceT;
import com.google.common.collect.Lists;

public class ServiceDaoTests extends AbstractTest {

    @Resource
    ServiceDao serviceDao;

    private static final String NAME = "TEST";

    private Service service = null;

    @Before
    public void init() {
        ServiceT svcT = new ServiceT();
        svcT.name = NAME;
        svcT.maxCores = 200;
        svcT.minCores = 200;
        svcT.maxRetries = 200;
        svcT.maxRam = 2048;
        svcT.minRam = 2048;
        svcT.tags = Lists.newArrayList("osx");

        service = serviceDao.create(svcT);
        verify(svcT);
    }

    @Test(expected=DataIntegrityViolationException.class)
    public void createDuplicateService() {
        ServiceT svcT = new ServiceT();
        svcT.name = NAME;
        svcT.maxCores = 200;
        svcT.minCores = 200;
        svcT.maxRetries = 200;
        svcT.maxRam = 2048;
        svcT.minRam = 2048;
        svcT.tags = Lists.newArrayList("osx");

        service = serviceDao.create(svcT);
    }

    @Test
    public void createServiceWithDisabledValues() {
         ServiceT svcT = new ServiceT();
         svcT.name = "test2";

         service = serviceDao.create(svcT);
         verify(svcT);
    }

    @Test
    public void deleteService() {
         ServiceT svcT = new ServiceT();
         svcT.name = "test2";

         service = serviceDao.create(svcT);
         verify(svcT);

         assertTrue(serviceDao.delete(service.getServiceId()));
    }

    @Test
    public void getServiceByName() {
        Service svc = serviceDao.get(service.getServiceId());
        assertEquals(service, svc);
    }

    @Test
    public void getServiceByUUID() {
        Service svc1 = serviceDao.get(service.getName());
        Service svc2 = serviceDao.get(NAME);
        assertEquals(service, svc1);
        assertEquals(service, svc2);
    }

    @Test
    public void updateService() {
        Service svc = serviceDao.get(service.getName());

        ServiceT svcT = new ServiceT();
        svcT.id = svc.getServiceId().toString();
        svcT.name = "maya";
        svcT.maxCores = 100;
        svcT.minCores = 100;
        svcT.maxRetries = 100;
        svcT.maxRam = 1024;
        svcT.minRam = 1024;
        svcT.tags = Lists.newArrayList("linux");
        svcT.threadable = true;

        serviceDao.update(svcT);
        verify(svcT);
    }

    @Test
    public void updateServiceWithDisabledValues() {
        Service svc = serviceDao.get(service.getName());

        ServiceT svcT = new ServiceT();
        svcT.id = svc.getServiceId().toString();
        svcT.name = "maya";

        serviceDao.update(svcT);
        verify(svcT);
    }

    public void verify(ServiceT service) {

        Map<String, Object> result = jdbc().queryForMap(
                "SELECT * FROM plow.service WHERE pk_service=?::uuid", service.id);

        assertEquals(service.name, result.get("str_name"));
        assertEquals(service.maxCores, result.get("int_cores_max"));
        assertEquals(service.minCores, result.get("int_cores_min"));
        assertEquals(service.maxRam, result.get("int_ram_max"));
        assertEquals(service.minRam, result.get("int_ram_min"));
        assertEquals(service.maxRetries, result.get("int_retries_max"));
        assertEquals(service.threadable, result.get("bool_threadable"));
        try {
            Jdbc4Array sqlArray = (Jdbc4Array) result.get("str_tags");
            if (sqlArray == null) {
                assertEquals(service.tags, null);
            }
            else {
                String[] array = (String[]) sqlArray.getArray();
                if (service.tags.size() == 0) {
                    assertEquals(0, array.length);
                }
                else {
                    assertEquals(service.tags.get(0), array[0]);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert Pg array " + e, e);
        }
    }
}
