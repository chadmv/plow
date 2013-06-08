package com.breakersoft.plow.test.thrift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.junit.Test;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.ClusterT;
import com.breakersoft.plow.thrift.JobT;
import com.breakersoft.plow.thrift.PlowException;
import com.breakersoft.plow.thrift.RpcService;
import com.breakersoft.plow.thrift.TaskFilterT;

public class RpcThriftServiceTests extends AbstractTest {

    @Resource
    RpcService.Iface rpcService;

    @Test
    public void testLaunch() throws PlowException, TException {
        rpcService.launch(getTestJobSpec());
    }

    @Test
    public void testCreateCluster() throws PlowException, TException {
        ClusterT cluster = rpcService.createCluster("bing");
        assertEquals("bing", cluster.name);
        assertTrue(cluster.tags.contains("bing"));
    }

    @Test
    public void testDeleteCluster() throws PlowException, TException {
        ClusterT cluster = rpcService.createCluster("bing");
        rpcService.deleteCluster(cluster.id);
    }

    @Test
    public void testGetProjects() throws PlowException, TException {
        rpcService.getProjects();
    }

    @Test
    public void testRetryTasks() throws PlowException, TException, InterruptedException {

        JobT job = rpcService.launch(getTestJobSpec());
        TaskFilterT filter = new TaskFilterT();
        //filter.setJobId(job.id);

        rpcService.retryTasks(filter);
        Thread.sleep(2000);
    }
}
