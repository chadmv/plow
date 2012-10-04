package com.breakersoft.plow;

public final class Defaults {

    public static int CORES_MIN = 1;
    public static int CORES_MAX = 16;

    public static int MEMORY_MIN_MB = 512;
    public static int MEMORY_MAX_MB = 16384;

    public static int MEMORY_RESERVE_MB = 512;

    public static String[] LAYER_TAG_DEFAULT = new String[] { "render" };

    public static String FOLDER_DEFAULT_NAME = "jobs";

    public static int DISPATCH_MAX_TASKS_PER_JOB = 8;
    public static int DISPATCH_BOOKING_THREADS = 4;
}
