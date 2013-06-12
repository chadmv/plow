package com.breakersoft.plow.monitor;

import java.lang.management.*;

public class JvmStats {

    private final static RuntimeMXBean runtimeMXBean;
    private final static MemoryMXBean memoryMXBean;
    private final static ThreadMXBean threadMXBean;

    static {

        runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        threadMXBean = ManagementFactory.getThreadMXBean();

    }

    public Memory mem;
    public String version;
    public long uptime;

    public static JvmStats getJvmStats() {

        JvmStats stats = new JvmStats();
        stats.version = runtimeMXBean.getVmVersion();
        stats.uptime = runtimeMXBean.getUptime();

        stats.mem = new Memory();

        MemoryUsage memUsage =  memoryMXBean.getHeapMemoryUsage();
        stats.mem.heapUsed = memUsage.getUsed() < 0 ? 0 : memUsage.getUsed();
        stats.mem.heapCommitted = memUsage.getCommitted() < 0 ? 0 : memUsage.getCommitted();

        memUsage = memoryMXBean.getNonHeapMemoryUsage();
        stats.mem.nonHeapUsed = memUsage.getUsed() < 0 ? 0 : memUsage.getUsed();
        stats.mem.nonHeapCommitted = memUsage.getCommitted() < 0 ? 0 : memUsage.getCommitted();

        return stats;
    }

    public static final class Memory {

        public long heapCommitted;
        public long heapUsed;
        public long nonHeapCommitted;
        public long nonHeapUsed;

        Memory() {
        }
    }

}
