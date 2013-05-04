package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.LayerSpecT;
import com.breakersoft.plow.thrift.TaskFilterT;
import com.breakersoft.plow.thrift.TaskState;

public class TaskDaoTests extends AbstractTest {

    @Resource
    TaskDao taskDao;

    @Resource
    LayerDao layerDao;

    @Resource
    JobDao jobDao;

    private Layer layer;

    private Task task;

    private Job job;

    @Test
    public void testCreate() {
        JobSpecT spec = getTestJobSpec();
        job = jobDao.create(TEST_PROJECT, spec);
        LayerSpecT lspec = spec.getLayers().get(0);
        layer = layerDao.create(job, lspec, 0);
        task = taskDao.create(layer, "0001-test", 1, 0, 0, 1, 512);
        taskDao.create(layer, "0002-test", 2, 1, 0, 1, 512);
        taskDao.create(layer, "0003-test", 3, 2, 0, 1, 512);
        taskDao.create(layer, "0004-test", 4, 3, 0, 1, 512);
        taskDao.create(layer, "0005-test", 5, 4, 0, 1, 512);
        taskDao.create(layer, "0006-test", 6, 5, 0, 1, 512);
    }

    @Test
    public void testGet() {
        testCreate();
        Task f1 = taskDao.get(task.getTaskId());
        assertEquals(task, f1);

        Task f2 = taskDao.get(layer, 1);
        assertEquals(task, f1);
        assertEquals(f2, f1);
    }

    @Test
    public void testGetByNameOrId() {
        testCreate();
        Task f1 = taskDao.get(task.getTaskId());
        assertEquals(task, f1);

        Task f2 = taskDao.getByNameOrId(job, f1.getTaskId().toString());
        assertEquals(task, f1);
        assertEquals(f2, f1);

        Task f3 = taskDao.getByNameOrId(job, "0001-test");
        assertEquals(task, f3);
        assertEquals(f2, f3);
    }

    @Test
    public void testUpdateState() {
        testCreate();
        assertTrue(taskDao.updateState(task,
                TaskState.INITIALIZE, TaskState.WAITING));
        assertFalse(taskDao.updateState(task,
                TaskState.DEAD, TaskState.RUNNING));
    }

    @Test
    public void testReserve() {
        testCreate();
        taskDao.start(task, 1, 1024);
    }

    @Test
    public void testStop() {
        testCreate();
        taskDao.stop(task, TaskState.SUCCEEDED);
    }

    @Test
    public void testStart() {
        testCreate();
        taskDao.reserve(task);
    }

    @Test
    public void testUnreserve() {
        testCreate();
        taskDao.unreserve(task);
    }

    @Test
    public void testResetTaskDispatchData() {
        testCreate();
        taskDao.resetTaskDispatchData(task, "foo");
    }

    @Test
    public void testUpdateTaskDispatchData() {

        testCreate();

        simpleJdbcTemplate.update("UPDATE task SET int_state=? WHERE pk_task=?",
                TaskState.RUNNING.ordinal(), task.getTaskId());

        RunningTask runtask = new RunningTask();
        runtask.taskId = task.getTaskId().toString();
        runtask.lastLog = "foo";
        runtask.rssMb = 999;
        runtask.cpuPercent = 90;
        runtask.progress = 50;
        taskDao.updateTaskDispatchData(runtask);

        Map<String,Object> data = simpleJdbcTemplate.queryForMap(
                "SELECT * FROM plow.task_ping WHERE pk_task=?",
                task.getTaskId());

        assertEquals(runtask.lastLog, (String) data.get("str_last_log_line"));
        assertEquals((Integer)runtask.rssMb, Integer.valueOf((int) data.get("int_rss")));
        assertEquals((Integer)runtask.rssMb, Integer.valueOf((int) data.get("int_max_rss")));
        assertEquals(runtask.cpuPercent, ((Integer) data.get("int_cpu_perc")).shortValue());
        assertEquals(runtask.cpuPercent, ((Integer) data.get("int_max_cpu_perc")).shortValue());
        assertEquals(50, data.get("int_progress"));
    }

    @Test
    public void getTasksWithTaskFilterA() {
        testCreate();
        TaskFilterT filter = new TaskFilterT();
        filter.setJobId(job.getJobId().toString());

        // Just job id.
        assertEquals(6, taskDao.getTasks(filter).size());

        filter.addToTaskIds(task.getTaskId().toString());
        assertEquals(1, taskDao.getTasks(filter).size());
    }

    @Test
    public void getTasksWithTaskFilterB() {
        testCreate();
        TaskFilterT filter = new TaskFilterT();
        filter.addToLayerIds(layer.getLayerId().toString());

        // Just layer ID
        assertEquals(6, taskDao.getTasks(filter).size());

        filter.addToStates(TaskState.RUNNING);
        assertEquals(0, taskDao.getTasks(filter).size());

        filter.addToStates(TaskState.INITIALIZE);
        assertEquals(6, taskDao.getTasks(filter).size());
    }

    @Test(expected=RuntimeException.class)
    public void getTasksWithTaskFilterC() {
        testCreate();
        TaskFilterT filter = new TaskFilterT();
        taskDao.getTasks(filter).size();
    }
}
