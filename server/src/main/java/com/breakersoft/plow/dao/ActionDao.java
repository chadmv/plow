package com.breakersoft.plow.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.ActionFull;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.thrift.ActionType;

public interface ActionDao {

    Action create(Filter filter, ActionType type, String value);

    Action get(UUID id);

    void delete(Action action);

    List<ActionFull> getAllFull(Filter filter);

    ActionFull getFull(Action action);

}
