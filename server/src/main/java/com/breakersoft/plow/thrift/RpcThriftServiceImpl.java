package com.breakersoft.plow.thrift;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.breakersoft.plow.Action;
import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Filter;
import com.breakersoft.plow.Folder;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Layer;
import com.breakersoft.plow.Matcher;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.event.JobLaunchEvent;
import com.breakersoft.plow.exceptions.PlowWriteException;
import com.breakersoft.plow.service.FilterService;
import com.breakersoft.plow.service.JobService;
import com.breakersoft.plow.service.NodeService;
import com.breakersoft.plow.service.ProjectService;
import com.breakersoft.plow.service.StateManager;
import com.breakersoft.plow.thrift.dao.ThriftActionDao;
import com.breakersoft.plow.thrift.dao.ThriftClusterDao;
import com.breakersoft.plow.thrift.dao.ThriftFilterDao;
import com.breakersoft.plow.thrift.dao.ThriftFolderDao;
import com.breakersoft.plow.thrift.dao.ThriftJobBoardDao;
import com.breakersoft.plow.thrift.dao.ThriftJobDao;
import com.breakersoft.plow.thrift.dao.ThriftLayerDao;
import com.breakersoft.plow.thrift.dao.ThriftMatcherDao;
import com.breakersoft.plow.thrift.dao.ThriftNodeDao;
import com.breakersoft.plow.thrift.dao.ThriftProjectDao;
import com.breakersoft.plow.thrift.dao.ThriftQuotaDao;
import com.breakersoft.plow.thrift.dao.ThriftTaskDao;

@ThriftService
public class RpcThriftServiceImpl implements RpcService.Iface {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(RpcThriftServiceImpl.class);

    @Autowired
    JobService jobService;

    @Autowired
    ProjectService projectService;

    @Autowired
    FilterService filterService;

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
    ThriftQuotaDao thriftQuotaDao;

    @Autowired
    ThriftFilterDao thriftFilterDao;

    @Autowired
    ThriftMatcherDao thriftMatcherDao;

    @Autowired
    ThriftActionDao thriftActionDao;

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
        return stateManager.killJob(jobService.getJob(UUID.fromString(jobId)), reason);
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
        jobService.setJobPaused(jobService.getJob(UUID.fromString(id)), value);
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
    public ProjectT createProject(String title, String code) throws PlowException, TException {
        Project project = projectService.createProject(title, code);
        return thriftProjectDao.get(project.getProjectId());
    }

    @Override
    public ProjectT getProject(String id) throws PlowException, TException {
        return thriftProjectDao.get(UUID.fromString(id));
    }

    @Override
    public List<ProjectT> getProjects() throws PlowException, TException {
        return thriftProjectDao.all();
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
    public ProjectT getProjectByCode(String code) throws PlowException,
            TException {
        return thriftProjectDao.get(code);
    }

    @Override
    public List<ProjectT> getActiveProjects() throws PlowException, TException {
        return thriftProjectDao.active();
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
        final Cluster cluster = nodeService.createCluster(name, tags);
        return thriftClusterDao.getCluster(cluster.getClusterId().toString());
    }

    @Override
    public boolean deleteCluster(String id) throws PlowException, TException {
        final Cluster c = nodeService.getCluster(UUID.fromString(id));
        return nodeService.deleteCluster(c);
    }

    @Override
    public boolean lockCluster(String id, boolean value) throws TException {
        final Cluster c = nodeService.getCluster(UUID.fromString(id));
        return nodeService.lockCluster(c, value);
    }

    @Override
    public void setClusterName(String id, String name) throws PlowException,
            TException {
        final Cluster c = nodeService.getCluster(UUID.fromString(id));
        nodeService.setClusterName(c, name);
    }

    @Override
    public void setClusterTags(String id, Set<String> tags)
            throws PlowException, TException {
        final Cluster c = nodeService.getCluster(UUID.fromString(id));
        nodeService.setClusterTags(c, tags);
    }

    @Override
    public void setDefaultCluster(String id) throws PlowException, TException {
        final Cluster c = nodeService.getCluster(UUID.fromString(id));
        nodeService.setDefaultCluster(c);
    }

    @Override
    public void retryTasks(TaskFilterT filter) throws PlowException, TException {
        stateManager.retryTasks(filter);
    }

    @Override
    public void eatTasks(TaskFilterT filter) throws PlowException, TException {
        stateManager.eatTasks(filter);
    }

    @Override
    public void killTasks(TaskFilterT filter) throws PlowException, TException {
        stateManager.killTasks(filter);
    }

    @Override
    public QuotaT getQuota(String arg0) throws PlowException, TException {
        return thriftQuotaDao.getQuota(UUID.fromString(arg0));
    }

    @Override
    public List<QuotaT> getQuotas(QuotaFilterT arg0) throws PlowException,
            TException {
        return thriftQuotaDao.getQuotas(arg0);
    }

    @Override
    public QuotaT createQuota(String projId, String clusterId, int size, int burst)
            throws PlowException, TException {
        Project proj = projectService.getProject(UUID.fromString(projId));
        Cluster clus = nodeService.getCluster(UUID.fromString(clusterId));
        Quota quota = nodeService.createQuota(proj, clus, size, burst);
        return thriftQuotaDao.getQuota(quota.getQuotaId());
    }

    @Override
    public void setQuotaBurst(String id, int value) throws PlowException,
            TException {
        Quota quota = nodeService.getQuota(UUID.fromString(id));
        nodeService.setQuotaBurst(quota, value);

    }

    @Override
    public void setQuotaLocked(String id, boolean value) throws PlowException,
            TException {
        Quota quota = nodeService.getQuota(UUID.fromString(id));
        nodeService.setQuotaLocked(quota, value);
    }

    @Override
    public void setQuotaSize(String id, int value) throws PlowException,
            TException {
        Quota quota = nodeService.getQuota(UUID.fromString(id));
        nodeService.setQuotaSize(quota, value);
    }

    @Override
    public void setLayerMaxCoresPerTask(String id, int cores)
            throws PlowException, TException {
        Layer layer = jobService.getLayer(UUID.fromString(id));
        jobService.setLayerMaxCores(layer, cores);
    }

    @Override
    public void setLayerMinCoresPerTask(String id, int cores)
            throws PlowException, TException {
        Layer layer = jobService.getLayer(UUID.fromString(id));
        jobService.setLayerMinCores(layer, cores);
    }

    @Override
    public void setLayerMinRamPerTask(String id, int ram)
            throws PlowException, TException {
        Layer layer = jobService.getLayer(UUID.fromString(id));
        jobService.setLayerMinRam(layer, ram);
    }

    @Override
    public void setLayerTags(String id, Set<String> tags)
            throws PlowException, TException {
        Layer layer = jobService.getLayer(UUID.fromString(id));
        jobService.setLayerTags(layer, tags);
    }

    @Override
    public void setLayerThreadable(String id, boolean threadable)
            throws PlowException, TException {
        Layer layer = jobService.getLayer(UUID.fromString(id));
        jobService.setLayerThreadable(layer, threadable);

    }

    @Override
    public void setProjectActive(String id, boolean value)
            throws PlowException, TException {
        Project project = projectService.getProject(UUID.fromString(id));
        projectService.setProjectActive(project, value);
    }

    @Override
    public void setJobMaxCores(String id, int cores) throws PlowException,
            TException {
        Job job = jobService.getJob(UUID.fromString(id));
        jobService.setJobMaxCores(job, cores);
    }

    @Override
    public void setJobMinCores(String id, int cores) throws PlowException,
            TException {
        Job job = jobService.getJob(UUID.fromString(id));
        jobService.setJobMinCores(job, cores);
    }

    @Override
    public void deleteFolder(String id) throws PlowException, TException {
        Folder folder = projectService.getFolder(UUID.fromString(id));
        projectService.deleteFolder(folder);
    }

    @Override
    public void setFolderMaxCores(String id, int cores) throws PlowException,
            TException {
        Folder folder = projectService.getFolder(UUID.fromString(id));
        projectService.setFolderMaxCores(folder, cores);
    }

    @Override
    public void setFolderMinCores(String id, int cores) throws PlowException,
            TException {
        Folder folder = projectService.getFolder(UUID.fromString(id));
        projectService.setFolderMinCores(folder, cores);
    }

    @Override
    public void setFolderName(String id, String name) throws PlowException,
            TException {
        Folder folder = projectService.getFolder(UUID.fromString(id));
        projectService.setFolderName(folder, name);
    }

    @Override
    public void setNodeLocked(String id, boolean value) throws PlowException,
            TException {
        Node node = nodeService.getNode(UUID.fromString(id));
        nodeService.setNodeLocked(node, value);
    }

    @Override
    public void setNodeCluster(String nodeId, String clusterId) throws PlowException,
            TException {
        Node node = nodeService.getNode(UUID.fromString(nodeId));
        if (nodeService.hasProcs(node)) {
            throw new PlowWriteException("You cannot move a Node with running procs.");
        }

        Cluster cluster = nodeService.getCluster(UUID.fromString(clusterId));
        // isolation=Serializable.
        nodeService.setNodeCluster(node, cluster);
    }

    @Override
    public void setNodeTags(String id, Set<String> tags)
            throws PlowException, TException {
        Node node = nodeService.getNode(UUID.fromString(id));
        nodeService.setTags(node, tags);
    }

    @Override
    public ActionT createAction(String filterId, ActionType type, String value)
            throws PlowException, TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        Action action = filterService.createAction(filter, type, value);
        return thriftActionDao.get(action.getActionId());
    }

    @Override
    public void deleteAction(String id) throws PlowException, TException {
        final Action action = filterService.getAction(UUID.fromString(id));
        filterService.deleteAction(action);
    }

    @Override
    public ActionT getAction(String id) throws PlowException, TException {
        return thriftActionDao.get(UUID.fromString(id));
    }

    @Override
    public List<ActionT> getActions(String filterId) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        return thriftActionDao.getAll(filter);
    }

    @Override
    public MatcherT createMatcher(String filterId, MatcherField field,
            MatcherType type, String value) throws PlowException, TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        Matcher matcher = filterService.createMatcher(filter, field, type, value);
        return thriftMatcherDao.get(matcher.getMatcherId());
    }

    @Override
    public void deleteMatcher(String matcherId) throws PlowException, TException {
        Matcher matcher = filterService.getMatcher(UUID.fromString(matcherId));
        filterService.deleteMatcher(matcher);
    }

    @Override
    public MatcherT getMatcher(String matcherId) throws PlowException, TException {
        return thriftMatcherDao.get(UUID.fromString(matcherId));
    }

    @Override
    public List<MatcherT> getMatchers(String filterId) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        return thriftMatcherDao.getAll(filter);
    }

    @Override
    public FilterT createFilter(String projectId, String name) throws PlowException, TException {
        Project project = projectService.getProject(UUID.fromString(projectId));
        Filter filter = filterService.createFilter(project, name);
        return thriftFilterDao.get(filter.getFilterId());
    }

    @Override
    public void deleteFilter(String filterId) throws PlowException, TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        filterService.deleteFilter(filter);
    }

    @Override
    public void decreaseFilterOrder(String filterId) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        filterService.decreaseFilterOrder(filter);
    }

    @Override
    public void increaseFilterOrder(String filterId) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        filterService.increaseFilterOrder(filter);
    }

    @Override
    public void setFilterName(String filterId, String name) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        filterService.setFilterName(filter, name);
    }

    @Override
    public void setFilterOrder(String filterId, int order) throws PlowException,
            TException {
        Filter filter = filterService.getFilter(UUID.fromString(filterId));
        filterService.setFilterOrder(filter, order);
    }

    @Override
    public List<FilterT> getFilters(String projectId) throws PlowException,
            TException {
        Project project = projectService.getProject(UUID.fromString(projectId));
        return thriftFilterDao.getAll(project);
    }

    @Override
    public FilterT getFilter(String filterId) throws PlowException, TException {
        return thriftFilterDao.get(UUID.fromString(filterId));
    }

    @Override
    public void setJobAttrs(String jobId, Map<String, String> attrs) throws PlowException,
            TException {
        Job job = jobService.getJob(UUID.fromString(jobId));
        jobService.setJobAttrs(job, attrs);
    }

    @Override
    public boolean dropDepend(String arg0) throws PlowException, TException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<DependT> getDependsOnJob(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DependT> getDependsOnLayer(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DependT> getDependsOnTask(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DependT> getJobDependsOn(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DependT> getLayerDependsOn(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<DependT> getTaskDependsOn(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean reactivateDepend(String arg0) throws PlowException,
            TException {
        // TODO Auto-generated method stub
        return false;
    }
}
