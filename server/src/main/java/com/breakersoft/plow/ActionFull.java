package com.breakersoft.plow;

import java.util.UUID;

import com.breakersoft.plow.thrift.ActionType;

public class ActionFull extends ActionE {

    public ActionType type;
    public String value;

    public UUID valueAsUUID() {
        return UUID.fromString(value);
    }

    public int valueAsInt() {
        return Integer.parseInt(value);
    }

    public boolean valueAsBool() {
        return Boolean.parseBoolean(value);
    }
}
