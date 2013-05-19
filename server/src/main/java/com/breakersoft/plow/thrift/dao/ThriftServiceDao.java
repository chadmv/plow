package com.breakersoft.plow.thrift.dao;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.thrift.ServiceT;

public interface ThriftServiceDao {

    ServiceT getService(String name);

    ServiceT getService(UUID id);

    List<ServiceT> getServices();

}
