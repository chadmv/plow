package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DependDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.thrift.JobSpecT;

public class DependDaoTests extends AbstractTest {

    @Resource
    DependDao dependDao;

    @Test
    public void testSatisfyDepend() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend depend = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        dependDao.satisfyDepend(depend);

        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT COUNT(1) FROM depend WHERE uuid_sig IS NULL"));
        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT COUNT(1) FROM depend WHERE bool_active='f'"));
    }

    @Test
    public void testUpdateDependCounts() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend depend = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        dependDao.incrementDependCounts(depend);

        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task"));

        dependDao.satisfyDepend(depend);
        dependDao.decrementDependCounts(depend);

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task"));
    }

    @Test
    public void testJobOnJob() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend depend = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        assertEquals(dspec.type, depend.getType());
        assertEquals(dspec.dependentJob, depend.getDependentJobId().toString());
        assertEquals(dspec.dependOnJob, depend.getDependOnJobId().toString());
        assertEquals(depend, dependDao.get(depend.getDependId()));
    }

    @Test
    public void testLayerOnLayer() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Layer dependentLayer =
                jobService.getLayer(event1.getJob(), 0);
        Layer dependOnLayer =
                jobService.getLayer(event2.getJob(), 0);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.LAYER_ON_LAYER;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();

        Depend depend = dependDao.createLayerOnLayer(
                dependentLayer, dependOnLayer);
        assertEquals(dspec.type, depend.getType());
        assertEquals(dspec.dependentJob, depend.getDependentJobId().toString());
        assertEquals(dspec.dependOnJob, depend.getDependOnJobId().toString());
        assertEquals(dspec.dependentLayer, depend.getDependentLayerId().toString());
        assertEquals(dspec.dependOnLayer, depend.getDependOnLayerId().toString());
        assertEquals(depend, dependDao.get(depend.getDependId()));
    }

    @Test
    public void testLayerOnTask() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Layer dependentLayer =
                jobService.getLayer(event1.getJob(), 0);
        Layer dependOnLayer =
                jobService.getLayer(event2.getJob(), 0);
        Task dependOnTask =
                jobService.getTask(dependOnLayer, 1);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.LAYER_ON_TASK;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependOnTask = dependOnTask.getTaskId().toString();

        Depend depend = dependDao.createLayerOnTask(
                dependentLayer, dependOnTask);
        assertEquals(dspec.type, depend.getType());
        assertEquals(dspec.dependentJob, depend.getDependentJobId().toString());
        assertEquals(dspec.dependOnJob, depend.getDependOnJobId().toString());
        assertEquals(dspec.dependentLayer, depend.getDependentLayerId().toString());
        assertEquals(dspec.dependOnLayer, depend.getDependOnLayerId().toString());
        assertEquals(dspec.dependOnTask, depend.getDependOnTaskId().toString());
        assertEquals(depend, dependDao.get(depend.getDependId()));
    }

    @Test
    public void testTaskOnLayer() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Layer dependentLayer =
                jobService.getLayer(event1.getJob(), 0);
        Layer dependOnLayer =
                jobService.getLayer(event2.getJob(), 0);
        Task dependentTask =
                jobService.getTask(dependentLayer, 1);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.TASK_ON_LAYER;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependentTask = dependentTask.getTaskId().toString();

        Depend depend = dependDao.createTaskOnLayer(
                dependentTask, dependOnLayer);
        assertEquals(dspec.type, depend.getType());
        assertEquals(dspec.dependentJob, depend.getDependentJobId().toString());
        assertEquals(dspec.dependOnJob, depend.getDependOnJobId().toString());
        assertEquals(dspec.dependentLayer, depend.getDependentLayerId().toString());
        assertEquals(dspec.dependOnLayer, depend.getDependOnLayerId().toString());
        assertEquals(dspec.dependentTask, depend.getDependentTaskId().toString());
        assertEquals(depend, dependDao.get(depend.getDependId()));
    }

    @Test
    public void testTaskOnTask() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Layer dependentLayer =
                jobService.getLayer(event1.getJob(), 0);
        Layer dependOnLayer =
                jobService.getLayer(event2.getJob(), 0);
        Task dependentTask =
                jobService.getTask(dependentLayer, 1);
        Task dependOnTask =
                jobService.getTask(dependOnLayer, 1);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.TASK_ON_TASK;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependentTask = dependentTask.getTaskId().toString();
        dspec.dependOnTask = dependOnTask.getTaskId().toString();

        Depend depend = dependDao.createTaskOnTask(
                dependentTask, dependOnTask);
        assertEquals(dspec.type, depend.getType());
        assertEquals(dspec.dependentJob, depend.getDependentJobId().toString());
        assertEquals(dspec.dependOnJob, depend.getDependOnJobId().toString());
        assertEquals(dspec.dependentLayer, depend.getDependentLayerId().toString());
        assertEquals(dspec.dependOnLayer, depend.getDependOnLayerId().toString());
        assertEquals(dspec.dependentTask, depend.getDependentTaskId().toString());
        assertEquals(dspec.dependOnTask, depend.getDependOnTaskId().toString());
        assertEquals(depend, dependDao.get(depend.getDependId()));
    }
}
