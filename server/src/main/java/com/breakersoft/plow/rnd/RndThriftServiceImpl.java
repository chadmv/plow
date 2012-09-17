package com.breakersoft.plow.rnd;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.rnd.thrift.Ping;
import com.breakersoft.plow.rnd.thrift.RndException;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;
import com.breakersoft.plow.service.RndService;

public class RndThriftServiceImpl implements RndServiceApi.Iface {

    @Autowired
    RndService rndService;

    @Override
    public void sendPing(Ping ping) throws RndException, TException {
        // TODO Auto-generated method stub

    }

}
