package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE implements DispatchResource {

    private int cores;
    private int memory;
    private boolean allocated;
    private Set<String> tags;

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

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public void allocate(int cores, int ram) {
        this.cores = this.cores - cores;
        this.memory = this.memory - ram;
    }
}
