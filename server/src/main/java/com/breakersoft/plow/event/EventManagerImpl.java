package com.breakersoft.plow.event;

import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public final class EventManagerImpl implements EventManager {

    private final EventBus eventBus;

    public EventManagerImpl() {
        eventBus = new EventBus();
    }

    @Override
    public void register(Object obj) {
        eventBus.register(obj);
    }

    @Override
    public void post(Object obj) {
        eventBus.post(obj);
    }
}

