package com.breakersoft.plow.test.dispatcher;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.dispatcher.DispatchService;
import com.breakersoft.plow.dispatcher.domain.DispatchJob;
import com.breakersoft.plow.dispatcher.domain.DispatchNode;
import com.breakersoft.plow.dispatcher.domain.DispatchProc;
import com.breakersoft.plow.dispatcher.domain.DispatchTask;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.JobSpecT;
import com.breakersoft.plow.thrift.SlotMode;

public class DispatchServiceTests extends AbstractTest {

    @Resource
    DispatchService dispatchService;

    @Resource
    NodeService nodeService;

    private List<DispatchTask> tasks;
    private DispatchNode node;

    @Before
    public void init() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        node = dispatchService.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        DispatchJob job = new DispatchJob(event.getJob());
        tasks = dispatchService.getDispatchableTasks(job, node);
    }

    @Test
    public void testAllocateDynamicProc() {
        DispatchTask task = tasks.get(0);

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(task.minCores, proc.getCores());
        assertEquals(task.minRam, proc.getRam());
    }

    @Test
    public void testAllocateSlotProc() {

        nodeService.setNodeSlotMode(node, SlotMode.SLOTS, 2, 4096);
        node = dispatchService.getDispatchNode(node.getName());

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(2, proc.getCores());
        assertEquals(4096, proc.getRam());
    }

    @Test
    public void testAllocateRuntSlotProc() {

        nodeService.setNodeSlotMode(node, SlotMode.SLOTS, 1, 4000);
        node = dispatchService.getDispatchNode(node.getName());

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(1, proc.getCores());
        assertEquals(4000, proc.getRam());

        // this is usually called by the dispatcher
        node.allocate(1, 4000);

        assertTrue(dispatchService.reserveTask(tasks.get(1)));
        proc = dispatchService.allocateProc(node, tasks.get(1));

        assertEquals(1, proc.getCores());
        assertEquals(3584, proc.getRam());
    }

    @Test
    public void testAllocateSingleResourceProc() {

        nodeService.setNodeSlotMode(node, SlotMode.SINGLE, 2, 4096);
        node = dispatchService.getDispatchNode(node.getName());

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(2, proc.getCores());
        assertEquals(8096 - Defaults.NODE_RESERVE_MEMORY, proc.getRam());
    }
}
