package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.JobId;
import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE implements DispatchResource, JobId {

    private UUID clusterId;
    private UUID quotaId;
    private int cores;
    private int ram;
    private boolean allocated;
    private boolean unbooked;
    private Set<String> tags;

    public String toString() {
        return String.format("Proc: %d/%d [%s] on host: %s", cores, ram, getProcId(), getHostname());
    }

    public int getIdleCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public int getIdleRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
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
        this.ram = this.ram - ram;
    }

    public boolean isUnbooked() {
        return unbooked;
    }

    public void setUnbooked(boolean unbooked) {
        this.unbooked = unbooked;
    }

    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public UUID getQuotaId() {
        return quotaId;
    }

    public void setQuotaId(UUID quotaId) {
        this.quotaId = quotaId;
    }
}
