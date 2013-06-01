package com.breakersoft.plow.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.dispatcher.dao.ProcDao;
import com.breakersoft.plow.rnd.thrift.RunningTask;

@Service
@Transactional
public class SchedulerServiceImpl implements SchedulerService {

}
