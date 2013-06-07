package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Depend;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.DependDao;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.DependSpecT;
import com.breakersoft.plow.thrift.DependT;
import com.breakersoft.plow.thrift.DependType;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.dao.ThriftDependDao;

public class ThriftDependDaoTests extends AbstractTest {

    @Resource
    DependDao dependDao;

    @Resource
    ThriftDependDao thriftDependDao;

    @Test
    public void testGetDepend() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend dependA = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        DependT dependT = thriftDependDao.getDepend(dependA.getDependId());
        assertEquals(dependA.getDependId().toString(), dependT.id);
    }

    @Test
    public void testGetWhatDependsOnJob() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend depend = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        List<DependT> depends1 = thriftDependDao.getWhatDependsOnJob(depend.getDependOnJobId());
        assertEquals(1, depends1.size());
    }

    @Test
    public void testGetWhatJobDependsOn() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.JOB_ON_JOB;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();

        Depend depend = dependDao.createJobOnJob(event1.getJob(), event2.getJob());
        List<DependT> depends = thriftDependDao.getWhatJobDependsOn(depend.getDependentJobId());
        assertEquals(1, depends.size());
    }

    @Test
    public void testGetWhatDependsOnLayer() {
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

        dependDao.createLayerOnLayer(
                event1.getJob(), dependentLayer, event2.getJob(), dependOnLayer);

        List<DependT> depends = thriftDependDao.getWhatDependsOnLayer(dependOnLayer.getLayerId());
        assertEquals(1, depends.size());
    }

    @Test
    public void testGetWhatLayerDependsOn() {
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

        dependDao.createLayerOnLayer(
                event1.getJob(), dependentLayer, event2.getJob(), dependOnLayer);

        List<DependT> depends = thriftDependDao.getWhatLayerDependsOn(dependentLayer.getLayerId());
        assertEquals(1, depends.size());
    }


    @Test
    public void testGetWhatDependsOnTask_Layer() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Job dependentJob = event1.getJob();
        Layer dependentLayer = jobService.getLayer(event1.getJob(), 0);

        Job dependOnJob = event1.getJob();
        Layer dependOnLayer = jobService.getLayer(event2.getJob(), 0);
        Task dependOnTask = jobService.getTask(dependOnLayer, 1);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.LAYER_ON_TASK;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependOnTask = dependOnTask.getTaskId().toString();

        dependDao.createLayerOnTask(
                dependentJob, dependentLayer, dependOnJob, dependOnLayer, dependOnTask);
        List<DependT> depends = thriftDependDao.getWhatDependsOnTask(dependOnTask.getTaskId());
        assertEquals(1, depends.size());

        depends = thriftDependDao.getWhatLayerDependsOn(dependentLayer.getLayerId());
        assertEquals(1, depends.size());
    }

    @Test
    public void testGetWhatDependsOnLayer_Task() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Job dependentJob = event1.getJob();
        Layer dependentLayer = jobService.getLayer(dependentJob, 0);
        Task dependentTask = jobService.getTask(dependentLayer, 1);

        Job dependOnJob = event2.getJob();
        Layer dependOnLayer = jobService.getLayer(dependOnJob, 0);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.TASK_ON_LAYER;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependentTask = dependentTask.getTaskId().toString();

        dependDao.createTaskOnLayer(
                dependentJob, dependentLayer, dependentTask, dependOnJob, dependOnLayer);

        List<DependT> depends = thriftDependDao.getWhatDependsOnLayer(dependOnLayer.getLayerId());
        assertEquals(1, depends.size());

    }

    @Test
    public void testGetWhatDependsOnTask() {
        JobSpecT spec1 = getTestJobSpec("depend_test_1");
        JobSpecT spec2 = getTestJobSpec("depend_test_2");

        JobLaunchEvent event1 = jobService.launch(spec1);
        JobLaunchEvent event2 = jobService.launch(spec2);

        Job dependentJob = event1.getJob();
        Layer dependentLayer = jobService.getLayer(dependentJob, 0);
        Task dependentTask = jobService.getTask(dependentLayer, 1);

        Job dependOnJob = event2.getJob();
        Layer dependOnLayer = jobService.getLayer(dependOnJob, 0);
        Task dependOnTask = jobService.getTask(dependOnLayer, 1);

        DependSpecT dspec = new DependSpecT();
        dspec.type = DependType.TASK_ON_TASK;
        dspec.dependentJob = event1.getJob().getJobId().toString();
        dspec.dependOnJob = event2.getJob().getJobId().toString();
        dspec.dependentLayer = dependentLayer.getLayerId().toString();
        dspec.dependOnLayer = dependOnLayer.getLayerId().toString();
        dspec.dependentTask = dependentTask.getTaskId().toString();
        dspec.dependOnTask = dependOnTask.getTaskId().toString();

        dependDao.createTaskOnTask(
                dependentJob, dependentLayer, dependentTask, dependOnJob, dependOnLayer, dependOnTask);

        List<DependT> depends = thriftDependDao.getWhatDependsOnTask(dependOnTask.getTaskId());
        assertEquals(1, depends.size());
    }

}
