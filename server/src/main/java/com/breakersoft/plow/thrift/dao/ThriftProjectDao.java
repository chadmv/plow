package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.ProjectT;

public interface ThriftProjectDao {

    ProjectT get(UUID id);

    ProjectT get(String name);

    long getPlowTime();

	List<ProjectT> all();

}
