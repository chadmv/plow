package com.breakersoft.plow;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PlowThreadPools {

    /**
     * The NodeDispatcherExecutor
     *
     * @return
     */
    @Bean(name="nodeDispatcherExecutor")
    public ThreadPoolTaskExecutor nodeDispatcherExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(16);
        t.setMaxPoolSize(16);
        t.setThreadNamePrefix("NodeDispatcher");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        return t;
    }

    /**
     * The ProcDispatcherExecutor
     *
     * @return
     */
    @Bean(name="procDispatcherExecutor")
    public ThreadPoolTaskExecutor procDispatcherExecutor() {
        ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
        t.setCorePoolSize(16);
        t.setMaxPoolSize(16);
        t.setThreadNamePrefix("ProcDispatcher");
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
        t.setCorePoolSize(16);
        t.setMaxPoolSize(32);
        t.setThreadNamePrefix("StateChange");
        t.setDaemon(false);
        t.setQueueCapacity(1000);
        t.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return t;
    }
}
