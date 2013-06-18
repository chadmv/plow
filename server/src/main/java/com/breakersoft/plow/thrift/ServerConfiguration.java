package com.breakersoft.plow.thrift;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.breakersoft.plow.PlowThreadPools;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;


@Configuration
public class ServerConfiguration {

    /*
     * RND server handles communication with
     * render nodes.
     */

    @Bean
    public ThriftServer getRndThriftServer() {
        return new ThriftServer(
                new RndServiceApi.Processor<RndServiceApi.Iface>(
                        getRndService()), new TBinaryProtocol.Factory(), PlowThreadPools.THIFT_RND_POOL_SIZE, 11337);
    }

    @Bean
    public RndServiceApi.Iface getRndService() {
        return new RndThriftServiceImpl();
    }

    /*
     * RPC server handles communication with
     * client tools.
     */


    @Bean
    public ThriftServer getRpcThriftServer() {
        return new ThriftServer(
                new RpcService.Processor<RpcService.Iface>(
                        getRpcService()), new TCompactProtocol.Factory(), 32, 11336);
    }

    @Bean
    public RpcService.Iface getRpcService() {
        return new RpcThriftServiceImpl();
    }
}
