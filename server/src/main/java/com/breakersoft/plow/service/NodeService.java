package com.breakersoft.plow.service;

import java.util.List;
import java.util.UUID;

import com.breakersoft.plow.Cluster;
import com.breakersoft.plow.Job;
import com.breakersoft.plow.Node;
import com.breakersoft.plow.Proc;
import com.breakersoft.plow.Project;
import com.breakersoft.plow.Quota;
import com.breakersoft.plow.rnd.thrift.Ping;

public interface NodeService {

    boolean nodeExists(String hostname);
    Node createNode(Ping ping);
    Node getNode(String hostname);
    void updateNode(Node node, Ping ping);
    Quota createQuota(Project project, Cluster cluster, int size, int burst);
    Quota createQuota(Project project, String cluster, int size, int burst);
	Cluster createCluster(String name, String[] tags);
    Cluster getCluster(String name);
    Cluster getDefaultCluster();
    void setDefaultCluster(Cluster cluster);

    List<Proc> getProcs(Job job);
    boolean setProcUnbooked(Proc proc, boolean unbooked);
	boolean deleteCluster(Cluster c);
	Cluster getCluster(UUID id);

}
