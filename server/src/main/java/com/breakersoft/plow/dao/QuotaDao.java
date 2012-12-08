package com.breakersoft.plow.dao;

import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;

public interface QuotaDao {

    Quota create(Project project, Cluster cluster, int size, int burst);

    Quota get(UUID id);

    Quota getQuota(Node node, Task task);

    boolean allocateResources(Quota quota, int cores);

    void freeResources(Quota quota, int cores);

    Quota getQuota(Proc proc);

}
