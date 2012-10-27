package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

import com.breakersoft.plow.TaskE;

public class DispatchTask extends TaskE {

    private String name;
    private int minCores;
    private int minMemory;
    private Set<String> tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinCores() {
        return minCores;
    }

    public void setMinCores(int minCores) {
        this.minCores = minCores;
    }

    public int getMinMemory() {
        return minMemory;
    }

    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}
