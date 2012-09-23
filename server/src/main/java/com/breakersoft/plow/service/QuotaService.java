package com.breakersoft.plow.service;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;

public interface QuotaService {

    Quota createQuota(Project project, Cluster cluster, int size, int burst);

    Quota createQuota(Project project, String cluster, int size, int burst);

}
