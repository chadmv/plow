package com.breakersoft.plow;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PlowThreadPools {

    public static final int THIFT_RND_POOL_SIZE = 24;

    @Bean(name="nodeDispatchExecutor")
    public ThreadPoolTaskExecutor nodeDispatcherExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(4);
        t.setMaxPoolSize(4);
        t.setThreadNamePrefix("nodeDispatchExecutor");
        t.setDaemon(false);
        t.setQueueCapacity(10000);
        return t;
    }

   @Bean(name="pipelineExecutor")
   public ThreadPoolTaskExecutor pipelineExecutor() {
       ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
       t.setCorePoolSize(48);
       t.setMaxPoolSize(48);
       t.setThreadNamePrefix("pipelineExecutor");
       t.setDaemon(false);
       t.setQueueCapacity(10000);
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
        t.setCorePoolSize(24);
        t.setMaxPoolSize(25);
        t.setThreadNamePrefix("RndRun");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        t.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return t;
    }
}
