package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import com.breakersoft.plow.ExitStatus;
import com.breakersoft.plow.Signal;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.dao.DispatchTaskDao;
import com.breakersoft.plow.dispatcher.dao.ProcDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.thrift.TaskStatsT;
import com.breakersoft.plow.thrift.TaskT;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;

public class ThriftTaskDaoTests extends AbstractTest {

    @Resource
    JobService jobService;

    @Resource
    ThriftTaskDao thriftTaskDao;

    @Resource
    DispatchTaskDao dispatchTaskDao;

    @Resource
    DispatchDao dispatchDao;

    @Resource
    ProcDao procDao;

    @Resource
    DispatchService dispatchService;


    @Test
    public void testGetTask() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        @SuppressWarnings("deprecation")
        UUID id = simpleJdbcTemplate.queryForObject(
                "SELECT pk_task FROM task LIMIT 1", UUID.class);
        TaskT task = thriftTaskDao.getTask(id);
        assertEquals(id, UUID.fromString(task.id));
    }

    @Test
    public void testGetTasks() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();

        List<TaskT> task = thriftTaskDao.getTasks(filter);
        assertTrue(task.size() > 0);
    }


    @Test
    public void testGetTasksByJob() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchTaskDao.getDispatchableTasks(job, node);

        assertTrue(dispatchTaskDao.reserve(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));
        dispatchTaskDao.start(tasks.get(0), proc);

        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();

        List<TaskT> task = thriftTaskDao.getTasks(filter);
        assertTrue(task.size() > 0);
    }

    @Test
    public void testGetRunningTasks() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchTaskDao.getDispatchableTasks(job, node);

        assertTrue(dispatchTaskDao.reserve(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));
        dispatchTaskDao.start(tasks.get(0), proc);

        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();
        filter.addToStates(TaskState.RUNNING);

        List<TaskT> task = thriftTaskDao.getTasks(filter);
        assertTrue(task.size() > 0);

        dispatchTaskDao.stop(tasks.get(0), TaskState.DEAD, 1, 1);

        task = thriftTaskDao.getTasks(filter);
        assertTrue(task.size() == 0);
    }

    @Test
    public void testGetTaskStats() throws InterruptedException {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchDao.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchTaskDao.getDispatchableTasks(job, node);

        Task t =  tasks.get(0);

        assertTrue(dispatchTaskDao.reserve(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));
        dispatchTaskDao.start(tasks.get(0), proc);

        List<TaskStatsT> stats = thriftTaskDao.getTaskStats(tasks.get(0).getTaskId());
        assertEquals(1, stats.size());

        Thread.sleep(1000);
        dispatchTaskDao.stop(t, TaskState.SUCCEEDED, ExitStatus.SUCCESS, Signal.NORMAL);

        stats = thriftTaskDao.getTaskStats(tasks.get(0).getTaskId());
        assertEquals(1, stats.size());
    }

    @Test
    public void testUpdatedTasks() throws InterruptedException {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        @SuppressWarnings("deprecation")
        UUID id = simpleJdbcTemplate.queryForObject(
                "SELECT pk_task FROM task LIMIT 1", UUID.class);
        Task t = jobService.getTask(id);
        jobService.setTaskState(t, TaskState.WAITING, TaskState.EATEN);

        TaskFilterT filter = new TaskFilterT();
        filter.jobId = event.getJob().getJobId().toString();
        filter.lastUpdateTime = System.currentTimeMillis() - 1000;

        List<TaskT> tasks = thriftTaskDao.getTasks(filter);
        assertEquals(1, tasks.size());

        filter.lastUpdateTime = System.currentTimeMillis();
        tasks = thriftTaskDao.getTasks(filter);
        assertEquals(0, tasks.size());
    }

    @Test
    public void getLogPath() {
        JobSpecT spec = getTestJobSpec();
        jobService.launch(spec);

        @SuppressWarnings("deprecation")
        UUID id = simpleJdbcTemplate.queryForObject(
                "SELECT pk_task FROM task ORDER BY int_task_order DESC LIMIT 1", UUID.class);

        String logPath = "/tmp/plow/unittests/test/0010-test_ls.-1.log";
        String result = thriftTaskDao.getLogPath(id);
        assertEquals(logPath, result);
    }
}
