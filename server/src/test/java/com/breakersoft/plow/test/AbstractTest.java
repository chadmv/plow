package com.breakersoft.plow.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.dao.ProjectDao;
import com.breakersoft.plow.json.Blueprint;
import com.breakersoft.plow.json.BlueprintLayer;
import com.breakersoft.plow.rnd.thrift.Hardware;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.google.common.collect.Lists;

@Transactional
@ContextConfiguration(locations={
        "file:src/main/webapp/WEB-INF/spring/root-context.xml"
    })
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {


    @Resource
    ProjectDao projectDao;

    protected Project testProject;

    @Before
    public void initTestProject() {
        testProject = projectDao.create("test", "Test");
    }

    public Blueprint getTestBlueprint() {

        Blueprint bp = new Blueprint();
        bp.setName("test");
        bp.setPaused(false);
        bp.setProject("test");
        bp.setScene("seq");
        bp.setShot("shot");
        bp.setUid(100);
        bp.setUsername("gandalf");

        BlueprintLayer layer = new BlueprintLayer();
        layer.setChunk(1);
        layer.setCommand(new String[] { "/bin/ls" });
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinMemory(1024);
        layer.setName("test_ls");
        layer.setRange("1-10");

        bp.addLayer(layer);

        return bp;
    }

    public  Ping getTestNodePing() {

        Hardware hw = new Hardware();
        hw.freeMemory = 4096;
        hw.freeSwap = 1024;
        hw.htFactor = 1;
        hw.osName = "OSX 10.8.1";
        hw.procModel = "Intel i7";
        hw.totalCores = 4;
        hw.totalMemory = 4096;
        hw.totalSwap = 1024;

        Ping ping = new Ping();
        ping.bootTime = System.currentTimeMillis() - 1000;
        ping.hostname = "test";
        ping.ipAddr = "127.0.0.1";
        ping.isReboot = true;
        ping.processes = Lists.newArrayList();
        ping.hw = hw;

        return ping;
    }

    @SuppressWarnings("deprecation")
    public void assertFrameCount(Job job, int count) {
        assertEquals(count, simpleJdbcTemplate.queryForInt(
                "SELECT COUNT(1) FROM plow.task, plow.layer " +
                "WHERE task.pk_layer = layer.pk_layer AND layer.pk_job=?", job.getJobId()));
    }

    @SuppressWarnings("deprecation")
    public void assertLayerCount(Job job, int count) {
        assertEquals(count, simpleJdbcTemplate.queryForInt(
                "SELECT COUNT(1) FROM plow.layer " +
                "WHERE layer.pk_job=?", job.getJobId()));
    }
}
