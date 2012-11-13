package com.breakersoft.plow.test.service;

import static org.junit.Assert.assertEquals;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.DependService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.TaskState;

public class DependServiceTests extends AbstractTest {

    @Resource
    DependService dependService;

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

        dependService.createDepend(dspec);

        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        dependService.satisfyDependsOn(event2.getJob());

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
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

        dependService.createDepend(dspec);

        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        dependService.satisfyDependsOn(dependOnLayer);

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
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

        dependService.createDepend(dspec);

        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        dependService.satisfyDependsOn(dependOnTask);

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));

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

        dependService.createDepend(dspec);

        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        dependService.satisfyDependsOn(dependOnLayer);

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
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

        dependService.createDepend(dspec);

        assertEquals(1,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        dependService.satisfyDependsOn(dependOnTask);

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));

        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT COUNT(1) FROM task WHERE int_state=?",
                TaskState.DEPEND.ordinal()));

    }

    @Test
    public void testTaskByTask() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Layer dependentLayer =
                jobService.getLayer(event1.getJob(), 0);
        Layer dependOnLayer =
                jobService.getLayer(event2.getJob(), 0);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.TASK_BY_TASK;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();

        dependService.createDepend(dspec);

        assertEquals(10,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event1.getJob().getJobId()));
        assertEquals(0,
                simpleJdbcTemplate.queryForInt("SELECT SUM(int_depend_count) FROM task WHERE pk_job=?",
                event2.getJob().getJobId()));

        // Satisifed handled by satisfyDependsOn(task)

    }
}
