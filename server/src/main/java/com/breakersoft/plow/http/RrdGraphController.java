package com.breakersoft.plow.http;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.DsType.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphConstants;
import org.rrd4j.graph.RrdGraphDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.breakersoft.plow.PlowCfg;
import com.breakersoft.plow.PlowThreadPools;
import com.breakersoft.plow.monitor.PlowStats;
import com.google.common.io.Files;

@Controller
public class RrdGraphController {

    private static final Logger logger = LoggerFactory.getLogger(RrdGraphController.class);

    private static final BasicStroke dottedStroke = new BasicStroke(
              1f,
              BasicStroke.CAP_ROUND,
              BasicStroke.JOIN_ROUND,
              1f,
              new float[] {2f},
              0f);

    private static final String PLOW_RRD = "plow.rrd";
    private static final String THREAD_RRD = "thread.rrd";

    @Autowired
    PlowThreadPools plowThreadPools;

    @Autowired
    PlowCfg plowCfg;

    private String rrdPath(String rrd) {
        return plowCfg.get("plow.rrd.path") + "/" + rrd;
    }

    @Scheduled(cron="0 * * * * *")
    public void poll() {

        if (!plowCfg.get("plow.rrd.enabled", false)) {
            return;
        }

        updatePlowRrd();
        updateThreadRrd();
    }

    @PostConstruct
    public void init() {

        if (!plowCfg.get("plow.rrd.enabled", false)) {
            logger.info("RRD graphs disabled.");
            return;
        }

        try {
            Files.createParentDirs(new File(rrdPath(PLOW_RRD)));
        } catch (IOException e1) {
            throw new RuntimeException("Failed to initialize RRD support: " + e1);
        }

        if (!new File(rrdPath(PLOW_RRD)).isFile()) {
            createPlowRrd();
        }

        if (!new File(rrdPath(THREAD_RRD)).isFile()) {
            createThreadRrd();
        }
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/node_dsp.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] nodeDispatcherStatsGraph() throws IOException {

        final String path = rrdPath(PLOW_RRD);
        final RrdGraphDef graphDef = baseGraph("Node Dispatcher Traffic", "dps/sec");

        graphDef.datasource("linea", path, "nodeDispatchHit", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "nodeDispatchMiss", ConsolFun.AVERAGE);
        graphDef.datasource("linec", path, "nodeDispatchFail", ConsolFun.AVERAGE);
        graphDef.area("linea", new Color(152, 175, 54), "Hit");
        graphDef.line("lineb", new Color(74, 104, 15), "Miss",1);
        graphDef.line("linec", new Color(164, 11, 23), "Error", 1);

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/proc_dsp.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] procDispatcherStatsGraph() throws IOException {

        final String path = rrdPath(PLOW_RRD);
        final RrdGraphDef graphDef = baseGraph("Proc Dispatcher Traffic", "dps/sec");

        graphDef.datasource("linea", path, "nodeDispatchHit", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "nodeDispatchMiss", ConsolFun.AVERAGE);
        graphDef.datasource("linec", path, "nodeDispatchFail", ConsolFun.AVERAGE);
        graphDef.area("linea", new Color(152, 175, 54), "Hit");
        graphDef.line("lineb", new Color(74, 104, 15), "Miss",1);
        graphDef.line("linec", new Color(164, 11, 23), "Error", 1);

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/task_exec.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] taskCountsGraph() throws IOException {

        final String path = rrdPath(PLOW_RRD);
        final RrdGraphDef graphDef = baseGraph("Task Executions", "task/sec");

        graphDef.datasource("linea", path, "taskStartedCount", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "taskStartedFailCount", ConsolFun.AVERAGE);
        graphDef.datasource("linec", path, "taskStoppedCount", ConsolFun.AVERAGE);
        graphDef.datasource("lined", path, "taskStoppedFailCount", ConsolFun.AVERAGE);
        graphDef.area("linea", new Color(152, 175, 54), "Started");
        graphDef.line("lineb", new Color(91, 40, 44), "Error Started",1);
        graphDef.area("linec", new Color(164, 11, 23), "Stopped");
        graphDef.line("lined", new Color(164, 11, 23), "Error Stopped", 1);

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/job_launch.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] jobLaunchGraph() throws IOException {

        final String path = rrdPath(PLOW_RRD);
        final RrdGraphDef graphDef = baseGraph("Job Launch", "jobs/sec");

        graphDef.datasource("linea", path, "jobLaunchCount", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "jobLaunchFailCount", ConsolFun.AVERAGE);
        graphDef.datasource("linec", path, "jobFinishCount", ConsolFun.AVERAGE);
        graphDef.datasource("lined", path, "jobKillCount", ConsolFun.AVERAGE);
        graphDef.area("linea", new Color(152, 175, 54), "Launched");
        graphDef.line("lineb", new Color(91, 40, 44), "Launch Fail",1);
        graphDef.area("linec", new Color(164, 11, 23), "Finished");
        graphDef.line("lined", new Color(191, 104, 15), "Jobs Killed", 1);

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/rnd_traffic.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] rndTrafficGraph() throws IOException {

        final String path = rrdPath(PLOW_RRD);
        final RrdGraphDef graphDef = baseGraph("Rnd Traffic", "ops/sec");

        graphDef.datasource("linea", path, "rndPingCount", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "rndTaskComplete", ConsolFun.AVERAGE);
        graphDef.area("linea", new Color(152, 175, 54), "Ping");
        graphDef.area("lineb", new Color(164, 11, 23), "Task Complete");

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/node_threads.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] nodeDispatcherThreads() throws IOException {

        final String path = rrdPath(THREAD_RRD);
        final RrdGraphDef graphDef = baseGraph("Node Dispatcher Queue", "threads/sec");

        graphDef.datasource("linea", path, "nodeActiveThreads", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "nodeWaiting", ConsolFun.AVERAGE);
        graphDef.line("linea", new Color(152, 175, 54), "Active Threads");
        graphDef.area("lineb", new Color(74, 104, 15), "Queued Work");

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/proc_threads.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] procDispatcherThreads() throws IOException {

        final String path = rrdPath(THREAD_RRD);
        final RrdGraphDef graphDef = baseGraph("Proc Dispatcher Queue", "threads/sec");

        graphDef.datasource("linea", path, "procActiveThreads", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "procWaiting", ConsolFun.AVERAGE);
        graphDef.line("linea", new Color(152, 175, 54), "Active Threads");
        graphDef.area("lineb", new Color(74, 104, 15), "Queued Work");

        return createImage(graphDef);
    }

    @ResponseBody
    @RequestMapping(value="/monitor/graphs/async_threads.png", method=RequestMethod.GET, produces=MediaType.IMAGE_PNG_VALUE)
    public byte[] asyncWorkQueueThreads() throws IOException {

        final String path = rrdPath(THREAD_RRD);
        final RrdGraphDef graphDef = baseGraph("Async Work Queue Threads", "threads/sec");

        graphDef.datasource("linea", path, "asyncActiveThreads", ConsolFun.AVERAGE);
        graphDef.datasource("lineb", path, "asyncWaiting", ConsolFun.AVERAGE);
        graphDef.line("linea", new Color(152, 175, 54), "Active Threads");
        graphDef.area("lineb", new Color(74, 104, 15), "Queued Work");

        return createImage(graphDef);
    }

    private byte[] createImage(RrdGraphDef graphDef) throws IOException {
        RrdGraph graph = new RrdGraph(graphDef);
        BufferedImage bim = new BufferedImage(graph.getRrdGraphInfo().getWidth(),
                graph.getRrdGraphInfo().getHeight(), BufferedImage.TYPE_INT_RGB);
        graph.render(bim.getGraphics());

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            ImageIO.write(bim, "png", baos);
            baos.flush();
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                baos.close();
            }
        }
    }

    private RrdGraphDef baseGraph(String title, String vertLabel) {
        final long now = System.currentTimeMillis() / 1000;
        RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setTimeSpan(now - 86400 , now);
        graphDef.setTitle(title);
        graphDef.setVerticalLabel(vertLabel);
        graphDef.setImageFormat("png");
        graphDef.setWidth(800);
        graphDef.setHeight(150);
        graphDef.setAntiAliasing(true);
        graphDef.setColor(RrdGraphConstants.COLOR_BACK, new Color(44, 44, 44));
        graphDef.setColor(RrdGraphConstants.COLOR_FONT, new Color(240, 240, 240));
        graphDef.setColor(RrdGraphConstants.COLOR_FRAME, new Color(55, 55, 55));
        graphDef.setColor(RrdGraphConstants.COLOR_CANVAS, new Color(55, 55, 55));
        graphDef.setColor(RrdGraphConstants.COLOR_GRID, new Color(82, 82, 82));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEA, new Color(44, 44, 44));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEB, new Color(44, 44, 44));
        graphDef.setGridStroke(dottedStroke);
        return graphDef;
    }

    public void updateThreadRrd() {
        RrdDb rrdDb;
         try {
             rrdDb = new RrdDb(rrdPath(THREAD_RRD));

             final String data = join(new long[] {
                 System.currentTimeMillis() / 1000,

                 plowThreadPools.nodeDispatcherExecutor().getThreadPoolExecutor().getPoolSize(),
                 plowThreadPools.nodeDispatcherExecutor().getActiveCount(),
                 plowThreadPools.nodeDispatcherExecutor().getThreadPoolExecutor().getCompletedTaskCount(),
                 (long) plowThreadPools.nodeDispatcherExecutor().getThreadPoolExecutor().getQueue().size(),
                 (long) plowThreadPools.nodeDispatcherExecutor().getThreadPoolExecutor().getQueue().remainingCapacity(),

                 plowThreadPools.procDispatcherExecutor().getThreadPoolExecutor().getPoolSize(),
                 plowThreadPools.procDispatcherExecutor().getActiveCount(),
                 plowThreadPools.procDispatcherExecutor().getThreadPoolExecutor().getCompletedTaskCount(),
                 (long) plowThreadPools.procDispatcherExecutor().getThreadPoolExecutor().getQueue().size(),
                 (long) plowThreadPools.procDispatcherExecutor().getThreadPoolExecutor().getQueue().remainingCapacity(),

                 plowThreadPools.stateChangeExecutor().getThreadPoolExecutor().getPoolSize(),
                 plowThreadPools.stateChangeExecutor().getActiveCount(),
                 plowThreadPools.stateChangeExecutor().getThreadPoolExecutor().getCompletedTaskCount(),
                 (long) plowThreadPools.stateChangeExecutor().getThreadPoolExecutor().getQueue().size(),
                 (long) plowThreadPools.stateChangeExecutor().getThreadPoolExecutor().getQueue().remainingCapacity()
             }, ":");

             logger.info("Writing Threads RRD sample {}", data);

             Sample sample = rrdDb.createSample();
             sample.setAndUpdate(data);
             rrdDb.close();

         } catch (Exception e) {
             logger.warn("Failed to write RRD file at {}, {}", rrdPath(THREAD_RRD), e);
         }
    }

    public void updatePlowRrd() {

         RrdDb rrdDb;
         try {
             rrdDb = new RrdDb(rrdPath(PLOW_RRD));
             final String data = join(new long[] {
                 System.currentTimeMillis() / 1000,
                 PlowStats.nodeDispatchHit.get(),
                 PlowStats.nodeDispatchMiss.get(),
                 PlowStats.nodeDispatchFail.get(),
                 PlowStats.procDispatchHit.get(),
                 PlowStats.procDispatchMiss.get(),
                 PlowStats.procDispatchFail.get(),
                 PlowStats.procAllocCount.get(),
                 PlowStats.procUnallocCount.get(),
                 PlowStats.procAllocFailCount.get(),
                 PlowStats.procUnallocFailCount.get(),
                 PlowStats.taskStartedCount.get(),
                 PlowStats.taskStartedFailCount.get(),
                 PlowStats.taskStoppedCount.get(),
                 PlowStats.taskStoppedFailCount.get(),
                 PlowStats.jobLaunchCount.get(),
                 PlowStats.jobLaunchFailCount.get(),
                 PlowStats.jobFinishCount.get(),
                 PlowStats.jobKillCount.get(),
                 PlowStats.rndPingCount.get(),
                 PlowStats.rndTaskComplete.get()
             },":");

             logger.info("Writing Plow RRD sample {}", data);

             Sample sample = rrdDb.createSample();
             sample.setAndUpdate(data);
             rrdDb.close();
         } catch (Exception e) {
             logger.warn("Failed to write Plow RRD file at {}, {}", rrdPath(PLOW_RRD), e);
         }
    }

    private void createThreadRrd() {

        logger.info("Initialzing Thread RRD data at: {}", rrdPath(THREAD_RRD));

        RrdDef rrdDef = new RrdDef(rrdPath(THREAD_RRD), 60);
        rrdDef.addArchive(AVERAGE, 0.5, 1, 1440);
        rrdDef.addArchive(AVERAGE, 0.5, 5, 288);
        rrdDef.addArchive(MAX, 0.5, 1, 1440);
        rrdDef.addArchive(MAX, 0.5, 5, 288);

        rrdDef.addDatasource("nodeThreads", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeActiveThreads", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeExecuted", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeWaiting", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeCapacity", GAUGE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("procThreads", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procActiveThreads", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procExecuted", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procWaiting", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procCapacity", GAUGE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("asyncThreads", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("asyncActiveThreads", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("asyncExecuted", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("asyncWaiting", GAUGE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("asyncCapacity", GAUGE, 600, Double.NaN, Double.NaN);

        saveRrdDef(rrdDef);
    }

    private void createPlowRrd() {

        logger.info("Initialzing Plow RRD data at: {}", rrdPath(PLOW_RRD));

        RrdDef rrdDef = new RrdDef(rrdPath(PLOW_RRD), 60);
        rrdDef.addArchive(AVERAGE, 0.5, 1, 1440);
        rrdDef.addArchive(AVERAGE, 0.5, 5, 288);
        rrdDef.addArchive(MAX, 0.5, 1, 1440);
        rrdDef.addArchive(MAX, 0.5, 5, 288);

        rrdDef.addDatasource("nodeDispatchHit", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeDispatchMiss", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("nodeDispatchFail", DERIVE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("procDispatchHit", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procDispatchMiss", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procDispatchFail", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procAllocCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procUnallocCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procAllocFailCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("procUnallocFailCount", DERIVE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("taskStartedCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("taskStartedFailCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("taskStoppedCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("taskStoppedFailCount", DERIVE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("jobLaunchCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("jobLaunchFailCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("jobFinishCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("jobKillCount", DERIVE, 600, Double.NaN, Double.NaN);

        rrdDef.addDatasource("rndPingCount", DERIVE, 600, Double.NaN, Double.NaN);
        rrdDef.addDatasource("rndTaskComplete", DERIVE, 600, Double.NaN, Double.NaN);

        saveRrdDef(rrdDef);
    }

    private void saveRrdDef(RrdDef rrdDef) {

        RrdDb rrdDb;
        try {
            rrdDb = new RrdDb(rrdDef);
            rrdDb.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize RRD support: " + e);
        }
    }

    public static String join(final long[] values, final String delimit) {
        StringBuilder sb = new StringBuilder(256);
        for (long value: values) {
            sb.append(String.valueOf(value));
            sb.append(delimit);
        }
        sb.delete(sb.length()-1, sb.length());
        return sb.toString();
    }
}
