package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.thrift.ActionT;

public interface ThriftActionDao {

	ActionT get(UUID id);

	List<ActionT> getAll(Filter filter);

}
