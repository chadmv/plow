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
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeService {

	/*
	 * Nodes
	 */

    boolean nodeExists(String hostname);
    Node createNode(Ping ping);
    Node getNode(String hostname);
    void updateNode(Node node, Ping ping);

    /*
     * Quotas
     */

    Quota createQuota(Project project, Cluster cluster, int size, int burst);
    Quota createQuota(Project project, String cluster, int size, int burst);

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
}
