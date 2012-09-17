package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE {

    private String frameName;
    private int number;
    private String[] command;
    private int minCores;
    private int minMemory;

    public String getFrameName() {
        return frameName;
    }

    public void setFrameName(String name) {
        this.frameName = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String[] getCommand() {
        return command;
    }

    public void setCommand(String[] command) {
        this.command = command;
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
}
