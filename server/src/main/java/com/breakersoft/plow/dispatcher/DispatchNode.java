package com.breakersoft.plow.dispatcher;

import java.util.Set;

import com.breakersoft.plow.NodeE;
import com.google.common.collect.ImmutableSet;

public class DispatchNode extends NodeE {

    private int idleCores;
    private int idleMemory;
    private ImmutableSet<String> tags;

    public DispatchNode() {

    }

    public int getIdleCores() {
        return idleCores;
    }

    public void setIdleCores(int idleCores) {
        this.idleCores = idleCores;
    }

    public int getIdleMemory() {
        return idleMemory;
    }

    public void setIdleMemory(int idleMemory) {
        this.idleMemory = idleMemory;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(ImmutableSet<String> tags) {
        this.tags = tags;
    }
}
