package com.breakersoft.plow.thrift;

import org.apache.thrift.TException;

@ThriftService
public class RpcThriftServiceImpl implements RpcServiceApi.Iface {

    @Override
    public JobT launch(JobBp arg0) throws PlowException, TException {
        System.out.println("bar");
        return null;
    }

}
