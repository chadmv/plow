package com.breakersoft.plow;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PlowThreadPools {

    @Autowired
    private PlowCfg plowCfg;

    public static final int THIFT_RND_POOL_SIZE = 32;

    @Bean(name="nodeDispatchExecutor")
    public ThreadPoolTaskExecutor nodeDispatcherExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(plowCfg.get("plow.dispatcher.node.threads", 4));
        t.setMaxPoolSize(plowCfg.get("plow.dispatcher.node.threads", 4));
        t.setThreadNamePrefix("nodeDispatchExecutor");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        return t;
    }

   @Bean(name="pipelineExecutor")
   public ThreadPoolTaskExecutor pipelineExecutor() {
       ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
       t.setCorePoolSize(plowCfg.get("plow.dispatcher.pipeline.threads", 16));
       t.setMaxPoolSize(plowCfg.get("plow.dispatcher.pipeline.threads", 16));
       t.setThreadNamePrefix("pipelineExecutor");
       t.setDaemon(false);
       t.setQueueCapacity(5000);
       return t;
   }

    /**
     * Handles Async commands from the API.
     */
    @Bean(name="stateChangeExecutor")
    public ThreadPoolTaskExecutor stateChangeExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(8);
        t.setMaxPoolSize(16);
        t.setThreadNamePrefix("StateChange");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        t.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return t;
    }

    /**
     * Handles communication with RNDaemon.
     */
    @Bean(name="rndCommandExecutor")
    public ThreadPoolTaskExecutor rndCommandExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(plowCfg.get("plow.rndpool.cache.threads", 8));
        t.setMaxPoolSize(plowCfg.get("plow.rndpool.cache.threads", 8));
        t.setThreadNamePrefix("RndRun");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        t.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return t;
    }
}
