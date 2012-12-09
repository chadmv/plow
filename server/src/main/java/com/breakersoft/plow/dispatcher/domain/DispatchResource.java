package com.breakersoft.plow.dispatcher.domain;

import java.util.Set;

public interface DispatchResource {

    public int getIdleCores();
    public int getIdleRam();
    public Set<String> getTags();
    public void allocate(int cores, int ram);
}
