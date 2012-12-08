package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.Defaults;
import com.breakersoft.plow.NodeE;

public class DispatchNode extends NodeE implements DispatchResource {

    private int cores;
    private int memory;
    private Set<String> tags;
    private boolean dispatchable;

    public DispatchNode() { }

    public int getIdleCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getIdleRam() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void decrement(int cores, int memory) {
        this.cores = this.cores - cores;
        this.memory = this.memory - memory;
    }

    public boolean isDispatchable() {
        if (cores == 0 || memory <= Defaults.MEMORY_MIN_MB) {
            return false;
        }
        return dispatchable;
    }

    public void setDispatchable(boolean dispatchable) {
        this.dispatchable = dispatchable;
    }
}
