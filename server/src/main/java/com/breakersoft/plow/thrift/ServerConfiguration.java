package com.breakersoft.plow.thrift;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.PlowCfg;
import com.breakersoft.plow.rnd.thrift.RndServiceApi;


@Configuration
public class ServerConfiguration {

    @Autowired
    private PlowCfg plowCfg;

    /*
     * RND server handles communication with
     * render nodes.
     */

    @Bean
    public ThriftServer getRndThriftServer() {
        return new ThriftServer(
                new RndServiceApi.Processor<RndServiceApi.Iface>(
                        getRndService()), new TBinaryProtocol.Factory(),
                        plowCfg.get("plow.rnd.network.threads", Defaults.RND_NETWORK_THREADS),
                        plowCfg.get("plow.rnd.network.port", Defaults.RND_NETWORK_PORT));
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
                        getRpcService()), new TCompactProtocol.Factory(),
                        plowCfg.get("plow.rpc.network.threads", Defaults.RPC_NETWORK_THREADS),
                        plowCfg.get("plow.rpc.network.port", Defaults.RPC_NETWORK_PORT));
    }

    @Bean
    public RpcService.Iface getRpcService() {
        return new RpcThriftServiceImpl();
    }
}
