package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.thrift.MatcherT;


public interface ThriftMatcherDao {

	MatcherT get(UUID id);

	List<MatcherT> getAll(Filter filter);

}
