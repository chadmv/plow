package com.breakersoft.plow.thrift;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.service.ProjectService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.breakersoft.plow.thrift.dao.ThriftFolderDao;
import com.breakersoft.plow.thrift.dao.ThriftJobBoardDao;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.breakersoft.plow.thrift.dao.ThriftNodeDao;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;

@ThriftService
public class RpcThriftServiceImpl implements RpcService.Iface {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcThriftServiceImpl.class);

    @Autowired
    JobService jobService;

    @Autowired
    ProjectService projectService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ThriftJobDao thriftJobDao;

    @Autowired
    ThriftLayerDao thriftLayerDao;

    @Autowired
    ThriftTaskDao thriftTaskDao;

    @Autowired
    ThriftNodeDao thriftNodeDao;

    @Autowired
    ThriftJobBoardDao thriftJobBoardDao;

    @Autowired
    ThriftProjectDao thriftProjectDao;

    @Autowired
    ThriftFolderDao thriftFolderDao;

    @Autowired
    ThriftClusterDao thriftClusterDao;

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
        return thriftNodeDao.getNode(UUID.fromString(id));
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

    @Override
    public ProjectT getProject(String id) throws PlowException, TException {
        return thriftProjectDao.get(UUID.fromString(id));
    }

    @Override
    public List<ProjectT> getProjects() throws PlowException, TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FolderT getFolder(String id) throws PlowException, TException {
        return thriftFolderDao.get(UUID.fromString(id));
    }

    @Override
    public List<FolderT> getJobBoard(String project) throws PlowException,
            TException {
        return thriftJobBoardDao.getJobBoard(UUID.fromString(project));
    }

    @Override
    public FolderT createFolder(String projectId, String name) throws PlowException,
            TException {
        Project project = projectService.getProject(UUID.fromString(projectId));
        Folder folder = projectService.createFolder(project, name);
        return thriftFolderDao.get(folder.getFolderId());
    }

    @Override
    public ProjectT getProjectByName(String name) throws PlowException,
            TException {
        return thriftProjectDao.get(name);
    }

    @Override
    public List<FolderT> getFolders(String projectId) throws PlowException,
            TException {
        Project project = projectService.getProject(UUID.fromString(projectId));
        return thriftFolderDao.getFolders(project);
    }

    @Override
    public long getPlowTime() throws PlowException, TException {
        return thriftProjectDao.getPlowTime();
    }

    @Override
    public String getTaskLogPath(String id) throws PlowException, TException {
        return thriftTaskDao.getLogPath(UUID.fromString(id));
    }

	@Override
	public ClusterT getCluster(String arg0) throws PlowException, TException {
		return thriftClusterDao.getCluster(arg0);
	}

	@Override
	public List<ClusterT> getClusters() throws PlowException, TException {
		return thriftClusterDao.getClusters();
	}

	@Override
	public List<ClusterT> getClustersByTag(String arg0) throws PlowException,
			TException {
		return thriftClusterDao.getClusters(arg0);
	}

	@Override
	public ClusterT createCluster(String name, Set<String> tags)
			throws PlowException, TException {
		final Cluster cluster = nodeService.createCluster(name, tags.toArray(new String[] {}));
		return thriftClusterDao.getCluster(cluster.getClusterId().toString());
	}

	@Override
	public boolean deleteCluster(String id) throws PlowException, TException {
		final Cluster c = nodeService.getCluster(UUID.fromString(id));
		return nodeService.deleteCluster(c);
	}
}
