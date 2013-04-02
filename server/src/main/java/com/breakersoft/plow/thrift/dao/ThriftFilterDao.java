package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Project;
import com.breakersoft.plow.thrift.FilterT;

public interface ThriftFilterDao {

	FilterT get(UUID id);

	List<FilterT> getAll(Project project);

}
