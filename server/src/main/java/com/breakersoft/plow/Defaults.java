
package com.breakersoft.plow;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ImmutableList;

public final class Defaults {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static final int RND_NETWORK_PORT = 11337;

    public static final int RND_NETWORK_THREADS = 8;

    public static final int RPC_NETWORK_PORT = 11336;

    public static final int RPC_NETWORK_THREADS = 8;

    public static final int JDBC_DEFAULT_BATCH_SIZE = 5000;

    /*
     * The amount of memory reserved on a node for operating
     * system processes. This is subtracted from the total
     * memory when a node is created.
     */
    public static int NODE_RESERVE_MEMORY = 512;

    public static String FOLDER_DEFAULT_NAME = "jobs";

    public static int RND_CLIENT_SOCKET_TIMEOUT_MS = 2000;

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


    /*
     * The highest possible value for the layer maxCores property.
     */
    public static int LAYER_MAX_MAX_CORES = 32767;

    /*
     * The lowest possible value for the layer minCores property.
     */
    public static int LAYER_MIN_MIN_CORES = 1;

    /*
     * The lowest possible value for the layer minRam property.
     */
    public static int LAYER_MIN_MIN_RAM = 256;

    /*
     * The highest possible value for the layer maxRam property.
     */
    public static int LAYER_MAX_MAX_RAM = 2147483647;


    // Amount of time a Node can go without communication before
    // plow determines the node is down.
    public static long NODE_UNRESPONSIVE_MS = 60000 * 5;
}
