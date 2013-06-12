package com.breakersoft.plow.http;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.PlowThreadPools;
import com.breakersoft.plow.monitor.JvmStats;
import com.google.common.collect.Maps;

/**
 *
 * A json dump of the server status.
 *
 * @author mchambers
 *
 */
@Controller
public class StatsController {

    @Autowired
    PlowThreadPools plowThreadPools;

    @RequestMapping(value = "/stats", method = RequestMethod.GET)
    public void home(HttpServletResponse response) throws IOException {

        final Map<String, Object> result = Maps.newLinkedHashMap();
        result.put("time", System.currentTimeMillis());
        result.put("jvm", JvmStats.getJvmStats());

        final Map<String, Object> pools =  Maps.newLinkedHashMap();
        pools.put("nodeDispatcher", collectThreadPoolStats(plowThreadPools.nodeDispatcherExecutor()));
        pools.put("procDispatcher", collectThreadPoolStats(plowThreadPools.procDispatcherExecutor()));
        result.put("threadpools", pools);

        Defaults.MAPPER.writeValue(response.getOutputStream(), result);
    }

    private Map<String,Object> collectThreadPoolStats(ThreadPoolTaskExecutor t) {
           Map<String,Object> stats = Maps.newHashMap();
           stats.put("threads", t.getThreadPoolExecutor().getPoolSize());
           stats.put("active", t.getActiveCount());
           stats.put("completed", t.getThreadPoolExecutor().getCompletedTaskCount());
           stats.put("queue", t.getThreadPoolExecutor().getQueue().size());
           stats.put("capacity", t.getThreadPoolExecutor().getQueue().remainingCapacity());
           stats.put("largest", t.getThreadPoolExecutor().getLargestPoolSize());
           return stats;
    }
}
