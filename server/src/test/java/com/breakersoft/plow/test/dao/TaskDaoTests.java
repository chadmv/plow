package com.breakersoft.plow.test.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.Task;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.dao.TaskDao;
import com.breakersoft.plow.dao.JobDao;
import com.breakersoft.plow.dao.LayerDao;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.Blueprint;
import com.breakersoft.plow.thrift.LayerBp;
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

    @Test
    public void testCreate() {
        Blueprint bp = getTestBlueprint();
        Job job = jobDao.create(testProject, bp);
        LayerBp bl = bp.job.getLayers().get(0);
        layer = layerDao.create(job, bl, 0);
        task = taskDao.create(layer, 1, 0, 0);
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
    public void testUpdateState() {
        testCreate();
        assertTrue(taskDao.updateState(task,
                TaskState.INITIALIZE, TaskState.WAITING));
        assertFalse(taskDao.updateState(task,
                TaskState.DEAD, TaskState.RUNNING));
    }
}
