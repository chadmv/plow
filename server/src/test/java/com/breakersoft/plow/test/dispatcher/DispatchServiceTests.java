package com.breakersoft.plow.test.dispatcher;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

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

    @Test
    public void testAllocateDynamicProc() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchService.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchService.getDispatchableTasks(job, node);
        DispatchTask task = tasks.get(0);

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(task.minCores, proc.getCores());
        assertEquals(task.minRam, proc.getRam());
    }

    @Test
    public void testAllocateSlotProc() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchService.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        nodeService.setNodeSlotMode(node, SlotMode.SLOTS, 2, 4096);

        node = dispatchService.getDispatchNode(node.getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchService.getDispatchableTasks(job, node);

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(2, proc.getCores());
        assertEquals(4096, proc.getRam());
    }

    @Test
    public void testAllocateSingleResourceProc() {
        JobSpecT spec = getTestJobSpec();
        JobLaunchEvent event = jobService.launch(spec);

        DispatchNode node = dispatchService.getDispatchNode(
                nodeService.createNode(getTestNodePing()).getName());

        nodeService.setNodeSlotMode(node, SlotMode.SINGLE, 2, 4096);
        node = dispatchService.getDispatchNode(node.getName());

        DispatchJob job = new DispatchJob(event.getJob());
        List<DispatchTask> tasks = dispatchService.getDispatchableTasks(job, node);

        assertTrue(dispatchService.reserveTask(tasks.get(0)));
        DispatchProc proc = dispatchService.allocateProc(node, tasks.get(0));

        assertEquals(2, proc.getCores());
        assertEquals(8096 - Defaults.NODE_RESERVE_MEMORY, proc.getRam());
    }
}
