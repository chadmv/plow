package com.breakersoft.plow;

public final class Defaults {

    public static int CORES_MIN = 1;
    public static int CORES_MAX = 16;

    public static int MEMORY_MIN_MB = 512;
    public static int MEMORY_MAX_MB = 16384;

    public static int MEMORY_RESERVE_MB = 512;

    public static String[] LAYER_TAG_DEFAULT = new String[] { "render" };

    public static String FOLDER_DEFAULT_NAME = "jobs";

    public static int RND_CLIENT_SOCKET_TIMEOUT_MS = 5000;

    public static int TASK_MAX_LIMIT = 1000;

    public static int PROC_ORPHANED_SECONDS = 300;
}
