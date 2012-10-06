package com.breakersoft.plow.test;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.rnd.thrift.Hardware;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.service.ProjectService;
import com.breakersoft.plow.service.QuotaService;
import com.breakersoft.plow.thrift.JobBp;
import com.breakersoft.plow.thrift.LayerBp;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Transactional
@ContextConfiguration(locations={
        "file:src/main/webapp/WEB-INF/spring/root-context.xml"
    })
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Resource
    ProjectService projectService;

    @Resource
    QuotaService quotaService;

    protected Project testProject;

    @Before
    public void initTestProject() {
        testProject = projectService.createProject("unittest", "Unit Test Project");
        quotaService.createQuota(testProject,"unassigned", 10, 15);
    }

    public JobBp getTestBlueprint() {

        JobBp bp = new JobBp();
        bp.setName("test");
        bp.setPaused(false);
        bp.setProject("unittest");
        bp.setUid(100);

        LayerBp layer = new LayerBp();
        layer.setChunk(1);
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinMemory(1024);
        layer.setName("test_ls");
        layer.setRange("1-10");
        layer.setTags(Sets.newHashSet("unassigned"));

        bp.addToLayers(layer);

        return bp;
    }

    public  Ping getTestNodePing() {

        Hardware hw = new Hardware();
        hw.cpuModel = "Intel i7";
        hw.platform = "OSX 10.8.1 x86_64";
        hw.freeRamMb = 4096;
        hw.freeSwapMb = 1024;
        hw.physicalCpus = 2;
        hw.totalRamMb = 4096;
        hw.totalSwapMb = 1024;

        Ping ping = new Ping();
        ping.bootTime = System.currentTimeMillis() - 1000;
        ping.hostname = "localhost";
        ping.ipAddr = "127.0.0.1";
        ping.isReboot = true;
        ping.tasks = Lists.newArrayList();
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
