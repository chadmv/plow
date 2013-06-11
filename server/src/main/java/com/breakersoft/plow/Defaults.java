
package com.breakersoft.plow;

import com.google.common.collect.ImmutableList;

public final class Defaults {

    public static int CORES_MIN = 1;
    public static int CORES_MAX = 16;

    public static int MEMORY_MIN_MB = 128;
    public static int MEMORY_MAX_MB = 16384;

    /*
     * The amount of memory reserved on a node for operating
     * system processes. This is subtracted from the total
     * memory when a node is created.
     */
    public static int NODE_RESERVE_MEMORY = 512;

    public static String[] LAYER_TAG_DEFAULT = new String[] { "render" };

    public static String FOLDER_DEFAULT_NAME = "jobs";

    public static int RND_CLIENT_SOCKET_TIMEOUT_MS = 5000;

    public static int TASK_MAX_LIMIT = 1000;

    public static int PROC_ORPHAN_CHECK_MILLIS = 300000;

    public static ImmutableList<String> DEFAULT_TAGS = ImmutableList.of("render");
    public static int DEFAULT_MIN_CORES = 1;
    public static int DEFAULT_MAX_CORES = 8;
    public static int DEFAULT_MIN_RAM = 3072;
    public static int DEFAULT_MAX_RAM = 20480;
    public static int DEFAULT_MAX_RETRIES = 2;
    public static boolean DEFAULT_THREADABLE = false;
    public static String DEFAULT_SERVICE = "default";

    // Amount of time a Node can go without communication before
    // plow determines the node is down.
    public static long NODE_UNRESPONSIVE_MS = 60000 * 5;
}
