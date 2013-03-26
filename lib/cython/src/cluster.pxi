
#######################
# ClusterCounts
#

cdef inline ClusterCounts initClusterCounts(ClusterCountsT& c):
    cdef ClusterCounts counts = ClusterCounts()
    counts.setClusterCounts(c)
    return counts

cdef class ClusterCounts:
    cdef:
        readonly int nodes, upNodes, downNodes, repairNodes
        readonly int lockedNodes, unlockedNodes, cores, upCores
        readonly int downCores, repairCores, lockedCores, unlockedCores
        readonly int runCores, idleCores

    cdef setClusterCounts(self, ClusterCountsT& c):
        self.nodes = c.nodes
        self.upNodes = c.upNodes
        self.downNodes = c.downNodes
        self.repairNodes = c.repairNodes
        self.lockedNodes = c.lockedNodes
        self.unlockedNodes = c.unlockedNodes
        self.cores = c.cores
        self.upCores = c.upCores
        self.downCores = c.downCores
        self.repairCores = c.repairCores
        self.lockedCores = c.lockedCores
        self.unlockedCores = c.unlockedCores
        self.runCores = c.runCores
        self.idleCores = c.idleCores



#######################
# Cluster
#

cdef inline Cluster initCluster(ClusterT& n):
    cdef Cluster cluster = Cluster()
    cluster.setCluster(n)
    return cluster


cdef class Cluster:

    cdef: 
        ClusterT _cluster
        ClusterCounts _total

    def __init__(self):
        self._total = None

    def __repr__(self):
        return "<Cluster: %s>" % self.name

    cdef setCluster(self, ClusterT& c):
        self._cluster = c
        self._total = None

    property id:
        def __get__(self): return self._cluster.id

    property name:
        def __get__(self): return self._cluster.name

    property isLocked:
        def __get__(self): return self._cluster.isLocked

    property isDefault:
        def __get__(self): return self._cluster.isDefault

    property tags:
        def __get__(self): return self._cluster.tags

    property total:
        def __get__(self): 
            cdef ClusterCounts c    
            if self._total is None:
                self._total = initClusterCounts(self._cluster.total)
            return self._total

    def delete(self):
        cdef bint ret 
        ret = delete_cluster(self.id)
        return ret

    def lock(self, bint locked):
        cdef bint ret 
        ret = lock_cluster(self.id, locked)
        return ret

    def set_tags(self, c_set[string] tags):
        set_cluster_tags(self.id, tags)
        self._cluster.tags = tags

    def set_name(self, string name):
        set_cluster_name(self.id, name)
        self._cluster.name = name

    def set_default(self):
        set_default_cluster(self.id)
        self._cluster.isDefault = True


def get_cluster(string name):
    cdef: 
        ClusterT clusterT 
        Cluster cluster 
    getClient().proxy().getCluster(clusterT, name)
    cluster = initCluster(clusterT)
    return cluster

def get_clusters():
    cdef: 
        ClusterT clusterT 
        vector[ClusterT] clusters 
        list ret = [] 
    getClient().proxy().getClusters(clusters)
    ret = [initCluster(clusterT) for clusterT in clusters]
    return ret    

def get_clusters_by_tag(string tag):
    cdef: 
        ClusterT clusterT 
        vector[ClusterT] clusters 
        list ret = [] 
    getClient().proxy().getClustersByTag(clusters, tag)
    ret = [initCluster(clusterT) for clusterT in clusters]
    return ret    

def create_cluster(str name, c_set[string] tags):
    cdef:
        ClusterT clusterT
        Cluster cluster 

    getClient().proxy().createCluster(clusterT, name, tags)
    cluster = initCluster(clusterT)
    return cluster

cpdef inline bint delete_cluster(Guid& clusterId):
    cdef bint ret
    ret = getClient().proxy().deleteCluster(clusterId)
    return ret

cpdef inline bint lock_cluster(Guid& clusterId, bint locked):
    cdef bint ret
    ret = getClient().proxy().lockCluster(clusterId, locked)
    return ret

cpdef inline set_cluster_tags(Guid& clusterId, list tags):
    pass

cpdef inline set_cluster_name(Guid& clusterId, string name):
    getClient().proxy().setClusterName(clusterId, name)

cpdef inline set_default_cluster(Guid& clusterId):
    getClient().proxy().setDefaultCluster(clusterId)



