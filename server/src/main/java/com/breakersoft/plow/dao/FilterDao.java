package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Project;

public interface FilterDao {

	Filter create(Project project, String name);

	boolean delete(Filter filter);

	void reorder(Project project);

	void setOrder(Filter filter, int order);

	void increaseOrder(Filter filter);

	void decreaseOrder(Filter filter);

	Filter get(Project project, int order);

	Filter get(UUID id);

	void setName(Filter filter, String name);
}
