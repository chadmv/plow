package com.breakersoft.plow.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


import com.breakersoft.plow.Service;
import com.breakersoft.plow.dao.ServiceDao;
import com.breakersoft.plow.thrift.ServiceT;

/**
 *
 * Handles most of the API most commonly used by wranglers/admins.
 *
 * @author chambers
 *
 */
@org.springframework.stereotype.Service
@Transactional
public class WranglerServiceImpl implements WranglerService {

    @Autowired
    ServiceDao serviceDao;

    @Override
    public Service createService(ServiceT service) {
        return serviceDao.create(service);
    }

    @Override
    public void updateService(ServiceT service) {
        serviceDao.update(service);
    }

    @Override
    public void deleteService(UUID id) {
        serviceDao.delete(id);
    }
}
