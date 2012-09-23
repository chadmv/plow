package com.breakersoft.plow.event;

import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

@Component
public final class EventManagerImpl implements EventManager {

    private final EventBus eventBus;
    private boolean enabled;

    public EventManagerImpl() {
        eventBus = new EventBus();
    }

    @Override
    public void register(Object obj) {
        eventBus.register(obj);
    }

    @Override
    public void post(Object obj) {
        if (isEnabled()) {
            eventBus.post(obj);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

