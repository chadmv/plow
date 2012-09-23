package com.breakersoft.plow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.dao.ClusterDao;
import com.breakersoft.plow.dao.QuotaDao;

@Transactional
@Service
public class QuotaServiceImpl implements QuotaService {

    @Autowired
    ClusterDao clusterDao;

    @Autowired
    QuotaDao quotaDao;

    @Override
    public Quota createQuota(Project project, Cluster cluster, int size, int burst) {
        return quotaDao.create(project, cluster, 10, 15);
    }

    @Override
    public Quota createQuota(Project project, String cluster, int size, int burst) {
        final Cluster c = clusterDao.getCluster(cluster);
        return quotaDao.create(project, c, 10, 15);
    }


}
