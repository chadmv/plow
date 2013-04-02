package com.breakersoft.plow.service;

import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.MatcherField;
import com.breakersoft.plow.thrift.MatcherType;

public interface FilterService {

	Filter createFilter(Project project, String name);

	Filter getFilter(UUID id);

	void setFilterName(Filter filter, String name);

	void setFilterOrder(Filter filter, int order);

	void increaseFilterOrder(Filter filter);

	void decreaseFilterOrder(Filter filter);

	boolean deleteFilter(Filter filter);

	/*
	 * Matchers
	 */
	Matcher createMatcher(Filter filter, MatcherField field, MatcherType type,
			String value);

	Matcher getMatcher(UUID id);

	boolean deleteMatcher(Matcher matcher);

}
