package com.breakersoft.plow.service;

import java.util.UUID;

import com.breakersoft.plow.Service;
import com.breakersoft.plow.thrift.ServiceT;

public interface WranglerService {

    Service createService(ServiceT service);

    void updateService(ServiceT service);

    void deleteService(UUID id);

}
