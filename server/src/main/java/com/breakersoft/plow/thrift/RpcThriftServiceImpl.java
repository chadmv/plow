package com.breakersoft.plow.thrift;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.breakersoft.plow.thrift.dao.ThriftNodeDao;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;

@ThriftService
public class RpcThriftServiceImpl implements RpcService.Iface {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcThriftServiceImpl.class);

    @Autowired
    JobService jobService;

    @Autowired
    ThriftJobDao thriftJobDao;

    @Autowired
    ThriftLayerDao thriftLayerDao;

    @Autowired
    ThriftTaskDao thriftTaskDao;

    @Autowired
    ThriftNodeDao thriftNodeDao;

    @Autowired
    StateManager stateManager;

    @Override
    public JobT launch(JobSpecT spec) throws PlowException, TException {

        logger.info("launchung job: {} ", spec);

        JobLaunchEvent event =  jobService.launch(spec);

        JobT result = new JobT();
        result.id = event.getJob().getJobId().toString();
        result.name = event.getJobSpec().getName();
        return result;
    }

    @Override
    public JobT getActiveJob(String name) throws PlowException, TException {
        return thriftJobDao.getRunningJob(name);
    }

    @Override
    public JobT getJob(String jobId) throws PlowException, TException {
        return thriftJobDao.getJob(jobId);
    }

    @Override
    public LayerT getLayerById(String id) throws PlowException, TException {
        return thriftLayerDao.getLayer(UUID.fromString(id));
    }

    @Override
    public List<LayerT> getLayers(String jobId) throws PlowException, TException {
        return thriftLayerDao.getLayers(UUID.fromString(jobId));
    }

    @Override
    public TaskT getTask(String id) throws PlowException, TException {
        return thriftTaskDao.getTask(UUID.fromString(id));
    }

    @Override
    public List<TaskT> getTasks(TaskFilterT filter) throws PlowException, TException {
        return thriftTaskDao.getTasks(filter);
    }

    @Override
    public List<JobT> getJobs(JobFilterT filter) throws PlowException, TException {
        return thriftJobDao.getJobs(filter);
    }

    @Override
    public boolean killJob(String jobId, String reason) throws PlowException, TException {
        return stateManager.killJob(jobService.getJob(jobId), reason);
    }

    @Override
    public NodeT getNode(String id) throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NodeT> getNodes(NodeFilterT filter) throws PlowException,
            TException {
        return thriftNodeDao.getNodes(filter);
    }

    @Override
    public void pauseJob(String id, boolean value) throws PlowException,
            TException {
        jobService.setJobPaused(jobService.getJob(id), value);
    }

    @Override
    public List<OutputT> getJobOutputs(String jobId) throws PlowException,
            TException {
        return thriftJobDao.getOutputs(UUID.fromString(jobId));
    }

    @Override
    public List<OutputT> getLayerOutputs(String layerId) throws PlowException,
            TException {
        return thriftLayerDao.getOutputs(UUID.fromString(layerId));
    }

    @Override
    public void addOutput(String layerId, String path, Map<String,String> attrs)
            throws PlowException, TException {
        jobService.addLayerOutput(
                jobService.getLayer(UUID.fromString(layerId)),
                path, attrs);
    }

    @Override
    public LayerT getLayer(String jobId, String name) throws PlowException,
            TException {
        return thriftLayerDao.getLayer(UUID.fromString(jobId), name);
    }
}
