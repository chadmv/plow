package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.NodeE;

public class DispatchNode extends NodeE implements DispatchResource {

    private int cores;
    private int memory;
    private Set<String> tags;

    public DispatchNode() { }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getMemory() {
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
}
