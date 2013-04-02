package com.breakersoft.plow.service;

import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Project;

public interface FilterService {

	Filter createFilter(Project project, String name);

	Filter getFilter(UUID id);

	void setFilterName(Filter filter, String name);

	void setFilterOrder(Filter filter, int order);

	void increaseFilterOrder(Filter filter);

	void decreaseFilterOrder(Filter filter);

	boolean deleteFilter(Filter filter);

}
