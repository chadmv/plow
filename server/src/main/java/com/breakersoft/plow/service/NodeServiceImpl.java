package com.breakersoft.plow.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
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
    public Cluster createCluster(String name, String[] tags) {
        return clusterDao.create(name, tags);
    }

    @Override
    public Cluster getCluster(String name) {
        return clusterDao.getCluster(name);
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
    public List<Proc> getProcs(Job job) {
        return procDao.getProcs(job);
    }

    @Override
    public boolean setProcUnbooked(Proc proc, boolean unbooked) {
        return procDao.setProcUnbooked(proc, unbooked);
    }
}
