package com.breakersoft.plow.test;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.rnd.thrift.Hardware;
import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.service.ProjectService;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.TaskSpecT;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "file:src/main/webapp/WEB-INF/spring/root-context.xml"
    })
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Resource
    protected ProjectService projectService;

    @Resource
    protected NodeService nodeService;

    @Resource
    protected JobService jobService;

    protected Project TEST_PROJECT;

    protected Cluster TEST_CLUSTER;

    protected Quota TEST_QUOTA;

    @Before
    public void initTestProject() {
        TEST_PROJECT = projectService.createProject("Unit Test Project", "unittest");
        TEST_CLUSTER = nodeService.createCluster("unittest", Sets.newHashSet("unassigned"));
        TEST_QUOTA = nodeService.createQuota(TEST_PROJECT, TEST_CLUSTER, 10, 15);

        nodeService.setDefaultCluster(TEST_CLUSTER);
    }

    public JobSpecT getTestJobSpec() {
        return getTestJobSpec("test");
    }

    public JobSpecT getTestJobSpec(String name) {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName(name);
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/" + name);

        LayerSpecT layer = new LayerSpecT();
        layer.setChunk(1);
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinRam(1024);
        layer.setName("test_ls");
        layer.setRange("1-10");
        layer.setTags(Lists.newArrayList("unassigned"));
        layer.env = Maps.newHashMap();
        layer.setServ(Defaults.DEFAULT_SERVICE);
        jobspec.addToLayers(layer);

        return jobspec;
    }

    public JobSpecT getTestJobSpec(String name, int layers) {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName(name);
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/" + name);

        for (int i=0; i<layers; i++) {

            LayerSpecT layer = new LayerSpecT();
            layer.setChunk(1);
            layer.setCommand(Lists.newArrayList("sleep", "5" ));
            layer.setMaxCores(8);
            layer.setMinCores(1);
            layer.setMinRam(1024);
            layer.setName(String.format("test_ls_%d", i));
            layer.setRange("1-10");
            layer.setTags(Lists.newArrayList("unassigned"));
            layer.env = Maps.newHashMap();
            layer.setServ(Defaults.DEFAULT_SERVICE);
            jobspec.addToLayers(layer);
        }

        return jobspec;
    }


    public JobSpecT getTestJobSpecManualTasks(String name) {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName(name);
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/" + name);

        LayerSpecT layer = new LayerSpecT();
        layer.setChunk(1);
        layer.setCommand(Lists.newArrayList("echo", "%{TASK}" ));
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinRam(1024);
        layer.setName("random_tasks");
        layer.setTags(Lists.newArrayList("unittest"));
        layer.setServ(Defaults.DEFAULT_SERVICE);

        jobspec.addToLayers(layer);

        TaskSpecT task = new TaskSpecT();
        task.name = "task1";
        task.depends = Lists.newArrayList();

        layer.addToTasks(task);

        return jobspec;
    }

    public JobSpecT getTestJobSpecWithAttrs(String name, Map<String,String> attrs) {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName(name);
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/" + name);
        jobspec.attrs = attrs;

        LayerSpecT layer = new LayerSpecT();
        layer.setChunk(1);
        layer.setCommand(Lists.newArrayList("echo", "%{TASK}" ));
        layer.setMaxCores(8);
        layer.setMinCores(1);
        layer.setMinRam(1024);
        layer.setName("random_tasks");
        layer.setTags(Lists.newArrayList("unittest"));
        layer.setServ(Defaults.DEFAULT_SERVICE);

        jobspec.addToLayers(layer);

        TaskSpecT task = new TaskSpecT();
        task.name = "task1";
        task.depends = Lists.newArrayList();

        layer.addToTasks(task);

        return jobspec;
    }

    public  Ping getTestNodePing() {

        Hardware hw = new Hardware();
        hw.cpuModel = "Intel i7";
        hw.platform = "OSX 10.8.1 x86_64";
        hw.freeRamMb = 4096;
        hw.freeSwapMb = 1024;
        hw.physicalCpus = 2;
        hw.totalRamMb = 4096;
        hw.totalSwapMb = 8096;

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
    public void assertTaskCount(Job job, int count) {
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

    @SuppressWarnings("deprecation")
    public SimpleJdbcTemplate jdbc() {
        return simpleJdbcTemplate;
    }
}
