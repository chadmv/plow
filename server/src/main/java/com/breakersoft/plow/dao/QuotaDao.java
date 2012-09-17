package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;

public interface QuotaDao {

    Quota create(Project project, Cluster cluster, int size, int burst);

    Quota get(UUID id);

}
