package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE implements DispatchResource {

    private String taskName;
    private String nodeName;

    private int cores;
    private int memory;
    private Set<String> tags;

    private UUID jobId;
    private UUID layerId;

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String name) {
        this.taskName = name;
    }

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

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public Set<String> getTags() {
        return tags;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        sb.append(cores);
        sb.append("/");
        sb.append(memory);
        sb.append(":");
        for (String tag: tags) {
            sb.append(tag);
            sb.append(",");
        }
        return sb.toString();
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getLayerId() {
        return layerId;
    }

    public void setLayerId(UUID layerId) {
        this.layerId = layerId;
    }
}
