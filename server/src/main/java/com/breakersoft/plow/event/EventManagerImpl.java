package com.breakersoft.plow.event;

import com.google.common.eventbus.EventBus;

public final class EventManagerImpl implements EventManager {

    private final EventBus eventBus;

    public EventManagerImpl() {
        eventBus = new EventBus();
    }
}

