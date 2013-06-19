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
import com.breakersoft.plow.monitor.PlowStats;
import com.breakersoft.plow.thrift.ClusterT;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.google.common.collect.Maps;

/**
 *
 * A json dump of the server status.
 *
 * @author mchambers
 *
 */
@Controller
public class MonitorController {

    @Autowired
    PlowThreadPools plowThreadPools;

    @Autowired
    ThriftClusterDao thriftClusterDao;

    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public void home(HttpServletResponse response) throws IOException {

        final Map<String, Object> result = Maps.newLinkedHashMap();
        result.put("time", System.currentTimeMillis());
        result.put("jvm", JvmStats.getJvmStats());

        final Map<String, Object> pools =  Maps.newLinkedHashMap();
        pools.put("node_dsp", collectThreadPoolStats(plowThreadPools.nodeDispatcherExecutor()));
        pools.put("pipeline_dsp", collectThreadPoolStats(plowThreadPools.pipelineExecutor()));
        pools.put("async_command", collectThreadPoolStats(plowThreadPools.stateChangeExecutor()));
        pools.put("rnd_command", collectThreadPoolStats(plowThreadPools.rndCommandExecutor()));

        result.put("threadpools", pools);

        final Map<String, Object> general =  Maps.newLinkedHashMap();
        result.put("general_stats", general);

        final Map<String, Object> dsp_node =  Maps.newLinkedHashMap();
        dsp_node.put("hit", PlowStats.nodeDispatchHit.get());
        dsp_node.put("miss", PlowStats.nodeDispatchMiss.get());
        dsp_node.put("fail", PlowStats.nodeDispatchFail.get());
        general.put("node_dsp", dsp_node);

        final Map<String, Object> dsp_proc =  Maps.newLinkedHashMap();
        dsp_proc.put("hit", PlowStats.procDispatchHit.get());
        dsp_proc.put("miss", PlowStats.procDispatchMiss.get());
        dsp_proc.put("fail", PlowStats.procDispatchFail.get());
        general.put("proc_dsp", dsp_proc);

        final Map<String, Object> rnd =  Maps.newLinkedHashMap();
        rnd.put("ping", PlowStats.rndPingCount.get());
        rnd.put("task_complete", PlowStats.rndTaskComplete.get());
        general.put("rnd", rnd);

        final Map<String, Object> procs =  Maps.newLinkedHashMap();
        procs.put("alloc", PlowStats.procAllocCount.get());
        procs.put("alloc_fail", PlowStats.procAllocFailCount.get());
        procs.put("unalloc", PlowStats.procUnallocCount.get());
        procs.put("unalloc_fail", PlowStats.procUnallocFailCount.get());
        procs.put("orphaned", PlowStats.procOrphanedCount.get());
        general.put("procs", procs);

        final Map<String, Object> tasks =  Maps.newLinkedHashMap();
        tasks.put("started", PlowStats.taskStartedCount.get());
        tasks.put("started_fail", PlowStats.taskStartedFailCount.get());
        tasks.put("stopped", PlowStats.taskStoppedCount.get());
        tasks.put("stopped_fail", PlowStats.taskStoppedFailCount.get());
        general.put("tasks", tasks);

        final Map<String, Object> jobs =  Maps.newLinkedHashMap();
        jobs.put("started", PlowStats.jobLaunchCount.get());
        jobs.put("started_fail", PlowStats.jobLaunchFailCount.get());
        jobs.put("stopped", PlowStats.jobFinishCount.get());
        jobs.put("killed", PlowStats.jobKillCount.get());
        general.put("jobs", jobs);

        final Map<String, Object> clusters =  Maps.newLinkedHashMap();

        for (ClusterT cluster: thriftClusterDao.getClusters()) {
            final Map<String, Object> cstats =  Maps.newLinkedHashMap();
            cstats.put("running", cluster.total.runCores);
            cstats.put("idle", cluster.total.idleCores);
            cstats.put("total", cluster.total.idleCores + cluster.total.runCores);
            clusters.put(cluster.name, cstats);
        }
        result.put("clusters", clusters);

        response.setContentType("application/json");
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
