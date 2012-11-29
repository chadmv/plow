package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;
import com.breakersoft.plow.thrift.TaskT;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;

public class ThriftTaskDaoTests extends AbstractTest {

    @Resource
    JobService jobService;

    @Resource
    ThriftTaskDao thriftTaskDao;

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
    public void testUpdatedTasks() throws InterruptedException {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        @SuppressWarnings("deprecation")
        UUID id = simpleJdbcTemplate.queryForObject(
                "SELECT pk_task FROM task LIMIT 1", UUID.class);
        Task t = jobService.getTask(id.toString());
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
}
