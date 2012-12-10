package com.breakersoft.plow.dispatcher;

import java.util.concurrent.atomic.AtomicBoolean;

public class DispatchConfig {

    /*
     * Maximum number of processes to start on a single
     * dispatching pass for a given node.
     */
    public static int MAX_PROCS_PER_JOB = 4;

    /*
     * Maximum number of cores to dispatch from a node per pass.
     * Can actually go over the max, but if you do then dispatching
     * switches to the next job.
     */
    public static int MAX_CORES_PER_JOB = 4;

    /*
     * Set to true once dispatching should start to happen.
     */
    public static AtomicBoolean IS_ENABLED = new AtomicBoolean(false);

}
