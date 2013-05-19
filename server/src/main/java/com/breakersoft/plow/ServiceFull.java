package com.breakersoft.plow;

import java.util.List;

public class ServiceFull extends ServiceE {

    private int minCores, maxCores, minRam, maxRam, maxRetries;
    private boolean threadable;
    private List<String> tags;

    private boolean setMinCores, setMaxCores, setMinRam, setMaxRam, setMaxRetries, setThreadable, setTags = false;

    public int getMinCores() {
        return minCores;
    }
    public void setMinCores(int minCores) {
        setMinCores = true;
        this.minCores = minCores;
    }
    public int getMaxCores() {
        return maxCores;
    }
    public void setMaxCores(int maxCores) {
        setMaxCores = true;
        this.maxCores = maxCores;
    }
    public int getMinRam() {
        return minRam;
    }
    public void setMinRam(int minRam) {
        setMinRam = true;
        this.minRam = minRam;
    }
    public int getMaxRam() {
        return maxRam;
    }
    public void setMaxRam(int maxRam) {
        setMaxRam = true;
        this.maxRam = maxRam;
    }
    public int getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(int maxRetries) {
        setMaxRetries = true;
        this.maxRetries = maxRetries;
    }
    public boolean isThreadable() {
        return threadable;
    }
    public void setThreadable(boolean threadable) {
        setThreadable = true;
        this.threadable = threadable;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        setTags = true;
        this.tags = tags;
    }
    public boolean isSetMinCores() {
        return setMinCores;
    }
    public boolean isSetMaxCores() {
        return setMaxCores;
    }
    public boolean isSetMinRam() {
        return setMinRam;
    }
    public boolean isSetMaxRam() {
        return setMaxRam;
    }
    public boolean isSetMaxRetries() {
        return setMaxRetries;
    }
    public boolean isSetThreadable() {
        return setThreadable;
    }
    public boolean isSetTags() {
        return setTags;
    }
}
