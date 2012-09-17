package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.TaskE;

public class DispatchTask extends TaskE {

    private String name;
    private int number;
    private String[] command;
    private int minCores;
    private int minMemory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
