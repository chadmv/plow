
#######################
# NodeState
#

@cython.internal
cdef class _NodeState:
    cdef:
        readonly int UP 
        readonly int DOWN 
        readonly int REPAIR 

    def __cinit__(self):
        self.UP = NODESTATE_UP
        self.DOWN = NODESTATE_DOWN
        self.REPAIR = NODESTATE_REPAIR

NodeState = _NodeState()


#######################
# NodeFilter
#

@cython.internal
cdef class NodeFilter:

    cdef NodeFilterT value

    def __init__(self, **kwargs):
        self.value.hostIds = kwargs.get('hostIds', [])
        self.value.clusterIds = kwargs.get('clusterIds', [])
        self.value.regex = kwargs.get('regex', '')
        self.value.hostnames = kwargs.get('hostnames', [])
        self.value.locked = kwargs.get('locked', False)

        cdef NodeState_type i
        for i in kwargs.get('states', []):
            self.value.states.push_back(i)  

#######################
# NodeSystem
#
cdef inline NodeSystem initNodeSystem(NodeSystemT t):
    cdef NodeSystem system = NodeSystem()
    system.cpuModel = t.cpuModel
    system.platform = t.platform
    system.physicalCores = t.physicalCores
    system.logicalCores = t.logicalCores
    system.totalRamMb = t.totalRamMb
    system.freeRamMb = t.freeRamMb
    system.totalSwapMb = t.totalSwapMb
    system.freeSwapMb = t.freeSwapMb
    system.load = t.load
    return system

cdef class NodeSystem:
    """
    Defines properties of the hardware specs of a Node
    at a sampled moment in time

    :var cpuModel: str 
    :var platform: str 
    :var physicalCores: int 
    :var logicalCores: int 
    :var totalRamMb: int 
    :var freeRamMb: int 
    :var totalSwapMb: int 
    :var freeSwapMb: int 
    :var load: list[int] 

    """
    cdef readonly string cpuModel, platform
    cdef readonly int physicalCores, logicalCores, totalRamMb
    cdef readonly int freeRamMb, totalSwapMb, freeSwapMb
    cdef vector[int] load

    property load:
        def __get__(self): return self.load


#######################
# Node
#

cdef inline Node initNode(NodeT& n):
    cdef Node node = Node()
    node.setNode(n)
    return node


cdef class Node:
    """
    Represents an existing Node (system)
    that can perform tasks

    :var id: str
    :var name: str
    :var clusterId: str
    :var clusterName: str
    :var ipaddr: str
    :var locked: bool
    :var createdTime: msec epoch timestamp
    :var updatedTime: msec epoch timestamp
    :var bootTime: msec epoch timestamp
    :var totalCores: int
    :var idleCores: int
    :var totalRamMb: int
    :var freeRamMb: int
    :var tags: set(str)
    :var state: :class:`.NodeState`
    :var system: :class:`.NodeSystem`

    """
    cdef NodeT _node

    def __repr__(self):
        return "<Node: %s>" % self.name

    cdef setNode(self, NodeT& n):
        self._node = n

    property id:
        def __get__(self): return self._node.id

    property name:
        def __get__(self): return self._node.name

    property clusterId:
        def __get__(self): return self._node.clusterId

    property clusterName:
        def __get__(self): return self._node.clusterName

    property ipaddr:
        def __get__(self): return self._node.ipaddr

    property locked:
        def __get__(self): return self._node.locked

    property createdTime:
        def __get__(self): return self._node.createdTime

    property updatedTime:
        def __get__(self): return self._node.updatedTime

    property bootTime:
        def __get__(self): return self._node.bootTime

    property totalCores:
        def __get__(self): return self._node.totalCores

    property idleCores:
        def __get__(self): return self._node.idleCores

    property totalRamMb:
        def __get__(self): return self._node.totalRamMb

    property freeRamMb:
        def __get__(self): return self._node.freeRamMb

    property tags:
        def __get__(self): return self._node.tags

    property state:
        def __get__(self): return self._node.state

    property system:
        def __get__(self): 
            cdef NodeSystem s
            s = initNodeSystem(self._node.system)
            return s

    cpdef refresh(self):
        """
        Refresh the attributes from the server
        """
        getClient().proxy().getNode(self._node, self._node.name)

    def set_locked(self, bint locked):
        """
        Set the lock state of the node 

        :param locked: bool 
        """
        set_node_locked(self, locked)

    def set_cluster(self, Guid& clusterId):
        """
        Assign the node to a cluster 

        :param clusterId: str :class:`.Cluster` id 
        """
        set_node_cluster(self, clusterId)

    def set_tags(self, c_set[string]& tags):
        """
        Set the tags for the node 

        :param tags: set(str)
        """
        set_node_tags(self, tags)


cpdef inline Node get_node(string name):
    """
    Get a node by name 

    :param name: str 
    :returns: :class:`.Node`
    """
    cdef: 
        NodeT nodeT
        Node node 

    getClient().proxy().getNode(nodeT, name)
    node = initNode(nodeT)
    return node

def get_nodes(**kwargs):
    """
    Get nodes matching keyword filter parameters

    :param hostIds: list[str :class:`.Node` id]
    :param cluster: list[str :class:`.Cluster`]
    :param regex: str 
    :param hostnames: list[str]
    :param locked: bool
    :returns: list[:class:`.Node`]
    """
    cdef:
        NodeT nodeT
        vector[NodeT] nodes 
        list ret
        NodeFilter filter = NodeFilter(**kwargs)
        NodeFilterT f = filter.value

    try:
        kwargs['cluster'] = [c.id for c in kwargs['cluster']]
    except:
        pass

    getClient().proxy().getNodes(nodes, f)
    ret = [initNode(nodeT) for nodeT in nodes]
    return ret

cpdef inline set_node_locked(Node node, bint locked) except? -1:
    """
    Set the lock state of the node 

    :param node: :class:`.Node`
    :param locked: bool 
    """
    getClient().proxy().setNodeLocked(node.id, locked)

cpdef inline set_node_cluster(Node node, Cluster cluster) except? -1:
    """
    Assign the node to a cluster

    :param node: :class:`.Node`
    :param cluster: :class:`.Cluster`
    """
    getClient().proxy().setNodeCluster(node.id, cluster.id)

cpdef inline set_node_tags(Node node, c_set[string]& tags) except? -1:
    """
    Set the tags for the node 

    :param node: :class:`.Node`
    :param tags: set(str) 
    """
    getClient().proxy().setNodeTags(node.id, tags)


