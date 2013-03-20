package com.breakersoft.plow.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.NodeDao;
import com.breakersoft.plow.dao.ProcDao;
import com.breakersoft.plow.dao.QuotaDao;
import com.breakersoft.plow.rnd.thrift.Ping;

@Service
@Transactional
public class NodeServiceImpl implements NodeService {

    @Autowired
    NodeDao nodeDao;

    @Autowired
    ClusterDao clusterDao;

    @Autowired
    ProcDao procDao;

    @Autowired
    QuotaDao quotaDao;

    @Override
    public boolean nodeExists(String hostname) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node createNode(Ping ping) {
        Cluster cluster = clusterDao.getDefaultCluster();
        return nodeDao.create(cluster, ping);
    }

    @Override
    public Node getNode(String hostname) {
        return nodeDao.get(hostname);
    }

    @Override
    public void updateNode(Node node, Ping ping) {
        // TODO Auto-generated method stub
    }

    @Override
    public Quota createQuota(Project project, Cluster cluster, int size, int burst) {
        return quotaDao.create(project, cluster, 10, 15);
    }

    @Override
    public Quota createQuota(Project project, String cluster, int size, int burst) {
        final Cluster c = clusterDao.getCluster(cluster);
        return quotaDao.create(project, c, 10, 15);
    }

    @Override
    public Quota getQuota(UUID id) {
    	return quotaDao.get(id);
    }

	@Override
	public void setQuotaSize(Quota quota, int size) {
		quotaDao.setSize(quota, size);
	}

	@Override
	public void setQuotaBurst(Quota quota, int burst) {
		quotaDao.setBurst(quota, burst);
	}

	@Override
	public void setQuotaLocked(Quota quota, boolean locked) {
		quotaDao.setLocked(quota, locked);
	}

    @Override
    public Cluster createCluster(String name, Set<String> tags) {
        return clusterDao.create(name, tags.toArray(new String[] {}));
    }

    @Override
    public boolean deleteCluster(Cluster c) {
        return clusterDao.delete(c);
    }

    @Override
    public Cluster getCluster(String name) {
        return clusterDao.getCluster(name);
    }

    @Override
    public Cluster getCluster(UUID id) {
        return clusterDao.getCluster(id);
    }

    @Override
    public Cluster getDefaultCluster() {
        return clusterDao.getDefaultCluster();
    }

    @Override
    public void setDefaultCluster(Cluster cluster) {
        clusterDao.setDefaultCluster(cluster);
    }

    @Override
    public boolean lockCluster(Cluster cluster, boolean value) {
        return clusterDao.setClusterLocked(cluster, value);
    }

    @Override
    public void setClusterName(Cluster cluster, String name) {
    	clusterDao.setClusterName(cluster, name);
    }

    @Override
    public void setClusterTags(Cluster cluster, Set<String> tags) {
    	clusterDao.setClusterTags(cluster, tags.toArray(new String[] {}));
    }

    @Override
    public List<Proc> getProcs(Job job) {
        return procDao.getProcs(job);
    }

    @Override
    @Transactional(readOnly=true)
    public Proc getProc(Task task) {
        return procDao.getProc(task);
    }

    @Override
    public boolean setProcUnbooked(Proc proc, boolean unbooked) {
        return procDao.setProcUnbooked(proc, unbooked);
    }
}
