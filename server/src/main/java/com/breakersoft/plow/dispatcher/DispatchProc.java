package com.breakersoft.plow.dispatcher;

import com.breakersoft.plow.ProcE;

public class DispatchProc extends ProcE {

    private String frameName;
    private int number;
    private int cores;
    private int memory;

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
}
