package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE implements DispatchResource {

    private String taskName;
    private String nodeName;

    private int cores;
    private int memory;
    private Set<String> tags;

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
}
