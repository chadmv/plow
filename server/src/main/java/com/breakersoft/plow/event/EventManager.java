package com.breakersoft.plow.event;

public interface EventManager {

    void register(Object obj);

    void post(Object obj);

}
