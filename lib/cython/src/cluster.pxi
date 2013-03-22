


#######################
# Cluster
#

cdef class Cluster:
    def __init__(self):
        raise NotImplementedError

# def get_cluster(string name):
#     cdef: 
#         ClusterT clusterT 
#         Cluster cluster 
#     getClient().proxy().getCluster(clusterT, name)
#     cluster = initCluster(clusterT)
#     return cluster

# def get_clusters():
#     cdef: 
#         ClusterT clusterT 
#         vector[ClusterT] clusters 
#         list ret = [] 
#     getClient().proxy().getClusters(clusters)
#     ret = [initCluster(clusterT) for clusterT in clusters]
#     return ret    

# def get_clusters_by_tag(string tag):
#     cdef: 
#         ClusterT clusterT 
#         vector[ClusterT] clusters 
#         list ret = [] 
#     getClient().proxy().getClusters(clusters, tag)
#     ret = [initCluster(clusterT) for clusterT in clusters]
#     return ret    

# def create_cluster(str name, list tags):
#     pass

def delete_cluster(Guid& clusterId):
    cdef bint ret
    ret = getClient().proxy().deleteCluster(clusterId)
    return ret

def lock_cluster(Guid& clusterId, bint locked):
    cdef bint ret
    ret = getClient().proxy().lockCluster(clusterId, locked)
    return ret

def set_cluster_tags(Guid& clusterId, list tags):
    pass

def set_cluster_name(Guid& clusterId, string name):
    getClient().proxy().setClusterName(clusterId, name)

def set_default_cluster(Guid& clusterId):
    getClient().proxy().setDefaultCluster(clusterId)



