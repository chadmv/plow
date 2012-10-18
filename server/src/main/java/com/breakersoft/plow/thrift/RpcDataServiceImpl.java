package com.breakersoft.plow.thrift;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.thrift.dao.ThriftJobDao;

@Service
@Transactional(readOnly = true)
public class RpcDataServiceImpl implements RpcDataService {

    @Autowired
    ThriftJobDao thriftJobDao;

    @Override
    public List<JobT> getJobs(JobFilter filter) {
        return thriftJobDao.getJobs(filter);
    }
}
