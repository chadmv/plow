package com.breakersoft.plow;

import java.util.Map;
import java.util.UUID;

public interface Output {

    public UUID getId();
    public String getPath();
    public Map<String,String> getAttrs();

}
