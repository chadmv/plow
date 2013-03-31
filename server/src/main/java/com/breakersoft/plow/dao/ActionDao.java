package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.thrift.ActionType;

public interface ActionDao {

	Action create(Filter filter, ActionType type, String value);

	Action getAction(UUID id);

}
