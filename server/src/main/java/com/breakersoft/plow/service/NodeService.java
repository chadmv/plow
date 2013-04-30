package com.breakersoft.plow.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.Task;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeService {

    /*
     * Nodes
     */
    Node createNode(Ping ping);
    Node getNode(String hostname);
    void updateNode(Node node, Ping ping);
    void setNodeLocked(Node node, boolean locked);
    Node getNode(UUID id);
    boolean hasProcs(Node node);
    void setNodeCluster(Node node, Cluster cluster);
    void setTags(Node node, Set<String> tags);

    /*
     * Quotas
     */

    Quota createQuota(Project project, Cluster cluster, int size, int burst);
    Quota createQuota(Project project, String cluster, int size, int burst);
    Quota getQuota(UUID id);
    void setQuotaSize(Quota quota, int size);
    void setQuotaBurst(Quota quota, int burst);
    void setQuotaLocked(Quota quota, boolean locked);

    /*
     * Clusters
     */

    Cluster createCluster(String name, Set<String> tags);
    Cluster getCluster(String name);
    Cluster getCluster(UUID id);
    Cluster getDefaultCluster();
    boolean deleteCluster(Cluster c);
    boolean lockCluster(Cluster cluster, boolean value);
    void setDefaultCluster(Cluster cluster);
    void setClusterTags(Cluster cluster, Set<String> tags);
    void setClusterName(Cluster cluster, String name);

    /*
     * Procs
     */

    List<Proc> getProcs(Job job);
    boolean setProcUnbooked(Proc proc, boolean unbooked);
    Proc getProc(Task task);
}
