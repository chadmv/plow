package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public interface MatcherDao {

	Matcher create(Filter filter, MatcherField field, MatcherType type,
			String value);

	void delete(Matcher matcher);

	void setValue(Matcher matcher, String value);
	void setField(Matcher matcher, MatcherField field);
	void setType(Matcher matcher, MatcherType type);

	void update(Matcher matcher, MatcherField field, MatcherType type,
			String value);

	Matcher get(UUID id);

}
