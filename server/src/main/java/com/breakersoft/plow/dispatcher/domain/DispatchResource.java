package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

public interface DispatchResource {

    public int getCores();
    public int getMemory();
    public Set<String> getTags();
}
