package com.breakersoft.plow;

import java.util.Map;
import java.util.UUID;

public class OutputE implements Output {

    private UUID id;
    private String path;
    private Map<String,String> attrs;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }
}
