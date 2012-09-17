package com.breakersoft.plow.dispatcher;

public interface Dispatchable {

    public float getTier();

    public boolean isDispatchable();

    public void incrementCores(int cores);

    public void decrementCores(int cores);

    public void recalculate();
}
