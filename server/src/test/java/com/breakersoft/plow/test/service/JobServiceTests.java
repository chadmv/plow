package com.breakersoft.plow.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.event.EventManager;
import com.breakersoft.plow.event.EventManagerImpl;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.WranglerService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.LayerT;
import com.breakersoft.plow.thrift.PlowException;
import com.breakersoft.plow.thrift.RpcService;
import com.breakersoft.plow.thrift.ServiceT;
import com.breakersoft.plow.thrift.TaskSpecT;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;

public class JobServiceTests extends AbstractTest {

    @Resource
    EventManager eventManager;

    @Resource
    WranglerService wranglerService;

    @Resource
    ThriftLayerDao thriftLayerDao;

    @Resource
    RpcService.Iface rpcService;


    private boolean jobLaunchEventHandled;

    @Before
    public void reset() {
        ((EventManagerImpl) eventManager).setEnabled(true);
        eventManager.register(this);
        jobLaunchEventHandled = false;
    }

    @Test
    public void testLaunchJob() {
        JobLaunchEvent event = jobService.launch(getTestJobSpec());
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 10);
        assertTrue(jobLaunchEventHandled);
    }

    @Test
    public void testLaunchJobWithManualTasks() {
        eventManager.register(this);
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecManualTasks("manual_test"));
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 1);
        assertTrue(jobLaunchEventHandled);
    }


    @Test
    public void testLaunchJobWithService() {

        ServiceT service = new ServiceT();
        service.setName(Defaults.DEFAULT_SERVICE);
        service.setMaxCores(200);
        service.setMinCores(2);
        service.setMaxRetries(12);
        service.setMaxRam(2048);
        service.setMinRam(2048);
        service.setTags(Lists.newArrayList("osx", "lion"));
        service.setThreadable(true);

        wranglerService.createService(service);

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName("service_test");
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/service_test");

        LayerSpecT layer = new LayerSpecT();
        layer.setName("foo");
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setRange("1-10");
        layer.setEnv(new HashMap<String,String>(0));
        layer.setServ(Defaults.DEFAULT_SERVICE);
        jobspec.addToLayers(layer);

        JobLaunchEvent event =
                jobService.launch(jobspec);

        LayerT lt = thriftLayerDao.getLayer(event.getJob().getJobId(), "foo");
        assertEquals(service.minCores, lt.minCores);
        assertEquals(service.maxCores, lt.maxCores);
        assertEquals(service.minRam, lt.minRam);
        assertEquals(service.maxRam, lt.maxRam);
        assertEquals(service.maxRetries, lt.maxRetries);
        assertEquals(service.tags, lt.getTags());
        assertEquals(service.threadable, lt.threadable);
    }

    @Test
    public void testLaunchJobWithTaskOnTaskDepends() {
        JobSpecT jobspec = new JobSpecT();
        jobspec.setName("task_on_task_depends");
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/service_test");

        LayerSpecT layer = new LayerSpecT();
        layer.setName("foo");
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setEnv(new HashMap<String,String>(0));
        layer.setServ(Defaults.DEFAULT_SERVICE);

        TaskSpecT task1 = new TaskSpecT();
        task1.name = "task_a";

        TaskSpecT task2 = new TaskSpecT();
        task2.name = "task_b";

        DependSpecT depend = new DependSpecT();
        depend.setDependentTask(task1.name);
        depend.setDependOnTask(task2.name);
        depend.setType(DependType.TASK_ON_TASK);

        task2.addToDepends(depend);
        layer.addToTasks(task1);
        layer.addToTasks(task2);
        jobspec.addToLayers(layer);

        JobLaunchEvent event =
                jobService.launch(jobspec);

        assertEquals(1, jdbc().queryForInt("SELECT int_depend_count FROM task WHERE pk_job=? AND str_name=?",
                event.getJob().getJobId(), task1.name));

    }

    @Test
    public void testLaunchJobNoService() {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName("service_test");
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/service_test");

        LayerSpecT layer = new LayerSpecT();
        layer.setName("foo");
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setRange("1-10");
        layer.setEnv(new HashMap<String,String>(0));
        layer.setServ(Defaults.DEFAULT_SERVICE);
        jobspec.addToLayers(layer);

        JobLaunchEvent event =
                jobService.launch(jobspec);

        LayerT lt = thriftLayerDao.getLayer(event.getJob().getJobId(), "foo");
        assertEquals(Defaults.DEFAULT_MIN_CORES, lt.minCores);
        assertEquals(Defaults.DEFAULT_MAX_CORES, lt.maxCores);
        assertEquals(Defaults.DEFAULT_MIN_RAM, lt.minRam);
        assertEquals(Defaults.DEFAULT_MAX_RAM, lt.maxRam);
        assertEquals(Defaults.DEFAULT_MAX_RETRIES, lt.maxRetries);
        assertEquals(Defaults.DEFAULT_TAGS, lt.getTags());
        assertEquals(Defaults.DEFAULT_THREADABLE, lt.threadable);
    }

    @Test
    public void testLaunchJobWithOverrides() {

        JobSpecT jobspec = new JobSpecT();
        jobspec.setName("service_test");
        jobspec.setUid(100);
        jobspec.setUsername("stella");
        jobspec.setPaused(false);
        jobspec.setProject("unittest");
        jobspec.setLogPath("/tmp/plow/unittests/service_test");

        LayerSpecT layer = new LayerSpecT();
        layer.setName("foo");
        layer.setCommand(Lists.newArrayList("sleep", "5" ));
        layer.setRange("1-10");
        layer.setEnv(new HashMap<String,String>(0));
        layer.setServ(Defaults.DEFAULT_SERVICE);

        layer.setMinCores(5);
        layer.setMaxCores(5);
        layer.setMaxRetries(5);
        layer.setMinRam(5);
        layer.setMaxRam(5);
        layer.setThreadable(true);
        layer.setTags(Lists.newArrayList("arse"));

        jobspec.addToLayers(layer);

        JobLaunchEvent event =
                jobService.launch(jobspec);

        LayerT lt = thriftLayerDao.getLayer(event.getJob().getJobId(), "foo");
        assertEquals(layer.getMinCores(), lt.minCores);
        assertEquals(layer.getMaxCores(), lt.maxCores);
        assertEquals(layer.getMinRam(), lt.minRam);
        assertEquals(layer.getMaxRam(), lt.maxRam);
        assertEquals(layer.getMaxRetries(), lt.maxRetries);
        assertEquals(layer.getTags(), lt.getTags());
        assertEquals(layer.isThreadable(), lt.threadable);
    }

    @Test
    public void testLaunchJobWithAttrs() throws PlowException, TException {

        Map<String,String> attrs = Maps.newHashMap();
        attrs.put("scene", "abc");
        attrs.put("shot", "123");

        eventManager.register(this);
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecWithAttrs("attrs_test", attrs));
        assertLayerCount(event.getJob(), 1);
        assertTaskCount(event.getJob(), 1);
        assertTrue(jobLaunchEventHandled);

        JobT job = rpcService.getJob(event.getJob().getJobId().toString());
        assertEquals("abc", job.attrs.get("scene"));
        assertEquals("123", job.attrs.get("shot"));

        attrs.clear();
        attrs.put("capt", "picard");
        attrs.put("cmdr", "riker");

        rpcService.setJobAttrs(job.id, attrs);

        job = rpcService.getJob(event.getJob().getJobId().toString());
        assertEquals("picard", job.attrs.get("capt"));
        assertEquals("riker", job.attrs.get("cmdr"));
    }

    @Test
    public void testLaunchJobPostTasks() {
        JobLaunchEvent event =
                jobService.launch(getTestJobSpecWithPostTasks("post_test"));
        //TODO fill in test
    }

    @Subscribe
    public void handleJobLaunchEvent(JobLaunchEvent event) {
        jobLaunchEventHandled = true;
    }
}
