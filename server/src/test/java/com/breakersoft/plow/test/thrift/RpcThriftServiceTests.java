package com.breakersoft.plow.test.thrift;

import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.junit.Test;

import com.breakersoft.plow.test.AbstractTest;
import com.breakersoft.plow.thrift.PlowException;
import com.breakersoft.plow.thrift.RpcService;

public class RpcThriftServiceTests extends AbstractTest {

    @Resource
    RpcService.Iface rpcService;

    @Test
    public void testLaunch() throws PlowException, TException {

        rpcService.launch(getTestJobSpec());
    }

}
