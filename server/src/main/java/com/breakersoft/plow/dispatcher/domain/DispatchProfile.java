package com.breakersoft.plow.dispatcher.domain;

/**
 * Controls some logic on how a particular project is dispatched.
 *
 * @author chambers
 *
 */
public class DispatchProfile {

    /*
     * True if new jobs have been added recently.
     */
    public boolean rapidTopologyChanges;

    /*
     * Add N more cores to every dispatch, increases as
     */
    public int plusCores;

    /*
     * Prefer more threads over more tasks. 0=tasks, 1=threads
     */
    public float preferThreads = 0.0f;

}
