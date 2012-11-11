package com.breakersoft.plow.test.thrift.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.NodeFilterT;
import com.breakersoft.plow.thrift.dao.ThriftNodeDao;

public class ThriftNodeDaoTests  extends AbstractTest {

    @Resource
    ThriftNodeDao thriftNodeDao;

    @Test
    public void testGetNodes() {
        Ping ping = getTestNodePing();
        nodeService.createNode(ping);
        assertEquals(1, thriftNodeDao.getNodes(new NodeFilterT()).size());
    }

}
