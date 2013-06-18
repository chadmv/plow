package com.breakersoft.plow.dispatcher.domain;

public class DispatchPair {

    public final DispatchProc proc;
    public final  DispatchTask task;

    DispatchPair(DispatchProc proc, DispatchTask task) {
        this.proc = proc;
        this.task = task;
    }
}
