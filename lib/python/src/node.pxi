
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

    def set_locked(self, bint locked):
        set_node_locked(self.id, locked)

    def set_cluster(self, Guid& clusterId):
        set_node_cluster(self.id, clusterId)

    def set_tags(self, c_set[string]& tags):
        set_node_tags(self.id, tags)


def get_node(string name):
    cdef: 
        NodeT nodeT
        Node node 

    getClient().proxy().getNode(nodeT, name)
    node = initNode(nodeT)
    return node

def get_nodes(**kwargs):
    cdef:
        NodeT nodeT
        vector[NodeT] nodes 
        list ret
        NodeFilter filter = NodeFilter(**kwargs)
        NodeFilterT f = filter.value

    getClient().proxy().getNodes(nodes, f)
    ret = [initNode(nodeT) for nodeT in nodes]
    return ret

cpdef inline set_node_locked(Guid& id, bint locked):
    getClient().proxy().setNodeLocked(id, locked)

cpdef inline set_node_cluster(Guid& id, Guid& clusterId):
    getClient().proxy().setNodeCluster(id, clusterId)

cpdef inline set_node_tags(Guid& id, c_set[string]& tags):
    getClient().proxy().setNodeTags(id, tags)


