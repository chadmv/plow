package com.breakersoft.plow;

public class Signal {

    /**
     * Exit signal for an aborted dispatch.
     */
    public static final int NORMAL = 0;

    /**
     * Exit signal for an aborted dispatch.
     */
    public static final int ABORTED_TASK = 667;

    /**
     * Exit signal for an orphaned task.
     */
    public static final int ORPANED_TASK = 668;

    /**
     * Task was manually killed
     */
    public static final int MANUAL_KILL = 669;

    /**
     * Task was manually retried
     */
    public static final int MANUAL_RETRY = 670;

    /**
     * The node went down.
     */
    public static final int NODE_DOWN = 671;

}
