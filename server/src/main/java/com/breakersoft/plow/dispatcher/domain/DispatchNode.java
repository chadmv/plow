package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.NodeE;
import com.breakersoft.plow.dispatcher.DispatchConfig;
import com.breakersoft.plow.thrift.SlotMode;

public class DispatchNode extends NodeE implements DispatchResource {

    private int cores;
    private int memory;
    private Set<String> tags;
    private boolean locked;
    private SlotMode slotMode;
    private int slotCores;
    private int slotRam;

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

    /**
     * Return true if the node has cores and memory available and
     * dispatchable is set to true.
     *
     * @return
     */
    public boolean isDispatchable() {
        if (cores == 0 || memory <= DispatchConfig.MIN_RAM_FOR_DISPATCH) {
            return false;
        }

        if (locked) {
            return false;
        }

        return true;
    }

    public String toString() {
        return String.format("Node: %s [%s] cores:%d mem:%d", getName(), getNodeId(), cores, memory);
    }

    @Override
    public void allocate(int cores, int ram) {
        this.cores = this.cores - cores;
        this.memory = this.memory - ram;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public SlotMode getSlotMode() {
        return slotMode;
    }

    public void setSlotMode(SlotMode slotMode) {
        this.slotMode = slotMode;
    }

    public int getSlotCores() {
        return slotCores;
    }

    public void setSlotCores(int slotCores) {
        this.slotCores = slotCores;
    }

    public int getSlotRam() {
        return slotRam;
    }

    public void setSlotRam(int slotRam) {
        this.slotRam = slotRam;
    }
}
