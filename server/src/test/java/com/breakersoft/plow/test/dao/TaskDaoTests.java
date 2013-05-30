package com.breakersoft.plow.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.dao.DispatchDao;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
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

    @Resource
    DispatchDao dispatchDao;

    @Resource
    DispatchService dispatchService;

    private Layer layer;

    private Task task;

    private Job job;

    @Test
    public void testCreate() {
        JobSpecT spec = getTestJobSpec();
        job = jobDao.create(TEST_PROJECT, spec, false);
        LayerSpecT lspec = spec.getLayers().get(0);
        layer = layerDao.create(job, lspec, 0);
        task = taskDao.create(layer, "0001-test", 1, 0, 0, 512);
        taskDao.create(layer, "0002-test", 2, 1, 0, 512);
        taskDao.create(layer, "0003-test", 3, 2, 0, 512);
        taskDao.create(layer, "0004-test", 4, 3, 0, 512);
        taskDao.create(layer, "0005-test", 5, 4, 0, 512);
        taskDao.create(layer, "0006-test", 6, 5, 0, 512);
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

    @Test
    public void getTasksWithTaskFilterUsingNode() {

        Node node =  nodeService.createNode(getTestNodePing());
        DispatchNode dnode = dispatchDao.getDispatchNode(node.getName());

        JobLaunchEvent event = jobService.launch(getTestJobSpec("proc_tests", 2));
        DispatchJob job = new DispatchJob(event.getJob());

        DispatchTask task = dispatchService.getDispatchableTasks(job, dnode).get(0);
        DispatchProc proc = dispatchService.allocateProc(dnode, task);


        TaskFilterT filter = new TaskFilterT();
        filter.addToNodeIds(proc.getNodeId().toString());

        List<Task> tasks = taskDao.getTasks(filter);
        assertEquals(1, tasks.size());
        assertEquals(task.getTaskId(), tasks.get(0).getTaskId());
    }

    @Test(expected=RuntimeException.class)
    public void getTasksWithTaskFilterC() {
        testCreate();
        TaskFilterT filter = new TaskFilterT();
        taskDao.getTasks(filter).size();
    }
}
