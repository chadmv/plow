package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.thrift.ServiceT;

public interface ServiceDao {

    Service get(UUID id);
    Service get(String name);
    Service create(ServiceT service);
    boolean update(ServiceT service);
    boolean delete(UUID id);
}
