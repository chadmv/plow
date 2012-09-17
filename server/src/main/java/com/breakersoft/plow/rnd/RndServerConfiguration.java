package com.breakersoft.plow.rnd;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.breakersoft.plow.rnd.thrift.RndServiceApi;

@Configuration
public class RndServerConfiguration {

    int port = 11337;

    @Bean
    public RndServer getServer() {
        return new RndServer(
                new RndServiceApi.Processor<RndServiceApi.Iface>(
                        getRndService()), port);
    }

    @Bean
    public RndServiceApi.Iface getRndService() {
        return new RndThriftServiceImpl();
    }
}
