import re 


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
# SlotMode
#

@cython.internal
cdef class _SlotMode:
    cdef:
        readonly int DYNAMIC 
        readonly int SINGLE
        readonly int SLOTS 

    def __cinit__(self):
        self.DYNAMIC = SLOTMODE_DYNAMIC
        self.SINGLE = SLOTMODE_SINGLE
        self.SLOTS = SLOTMODE_SLOTS

SlotMode = _SlotMode()


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
    system.cpuModel = re.sub(r' +', ' ', t.cpuModel)
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


cdef class Node(PlowBase):
    """
    Represents an existing Node (system)
    that can perform tasks

    :var id: str
    :var name: str
    :var clusterId: str
    :var clusterName: str
    :var ipaddr: str
    :var locked: bool
    :var createdTime: long msec epoch timestamp
    :var updatedTime: long msec epoch timestamp
    :var bootTime: long msec epoch timestamp
    :var totalCores: int
    :var idleCores: int
    :var slotCores: int
    :var totalRamMb: int
    :var freeRamMb: int
    :var slotRam: int
    :var tags: set(str)
    :var state: :obj:`.NodeState`
    :var system: :class:`.NodeSystem`
    :var mode: :data:`.SlotMode`

    """
    cdef NodeT node
    cdef NodeSystem _system

    def __init__(self):
        self._system = None

    def __repr__(self):
        return "<Node: %s>" % self.name

    cdef setNode(self, NodeT& n):
        self.node = n
        self._system = initNodeSystem(self.node.system)

    property id:
        def __get__(self): return self.node.id

    property name:
        def __get__(self): return self.node.name

    property clusterId:
        def __get__(self): return self.node.clusterId

    property clusterName:
        def __get__(self): return self.node.clusterName

    property ipaddr:
        def __get__(self): return self.node.ipaddr

    property locked:
        def __get__(self): return self.node.locked

    property createdTime:
        def __get__(self): return long(self.node.createdTime)

    property updatedTime:
        def __get__(self): return long(self.node.updatedTime)

    property bootTime:
        def __get__(self): return long(self.node.bootTime)

    property totalCores:
        def __get__(self): return self.node.totalCores

    property idleCores:
        def __get__(self): return self.node.idleCores

    property slotCores:
        def __get__(self): return self.node.slotCores

    property totalRamMb:
        def __get__(self): return self.node.totalRamMb

    property freeRamMb:
        def __get__(self): return self.node.freeRamMb

    property slotRam:
        def __get__(self): return self.node.slotRam

    property tags:
        def __get__(self): return self.node.tags

    property state:
        def __get__(self): return self.node.state

    property system:
        def __get__(self): return self._system

    property mode:
        def __get__(self): return self.node.mode

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef NodeT node 
        conn().proxy().getNode(node, self.node.name)
        self.setNode(node)

    def lock(self, bint locked):
        """
        Set the lock state of the node 

        :param locked: bool 
        """
        set_node_locked(self, locked)

    def get_cluster(self):
        """
        Get the cluster object to which this node is assigned

        :returns: :class:`.Cluster`
        """
        cdef Cluster c 
        if self.clusterName:
            c = get_cluster(self.clusterName)
            return c

        return None

    def set_cluster(self, Cluster cluster):
        """
        Assign the node to a cluster 

        :param cluster: str :class:`.Cluster`
        """
        set_node_cluster(self, cluster)

    def set_tags(self, c_set[string]& tags):
        """
        Set the tags for the node 

        :param tags: set(str)
        """
        set_node_tags(self, tags)

    def set_slot_mode(self, int mode, int cores, int ram):
        """
        Set the slot mode for a node

        :param mode: :data:`.SlotMode`
        :param cores: int number of cores
        :param ram: int ram in MB
        """
        set_node_slot_mode(self, mode, cores, ram)


@reconnecting
def get_node(string name):
    """
    Get a node by name 

    :param name: str 
    :returns: :class:`.Node`
    """
    cdef: 
        NodeT nodeT
        Node node 

    conn().proxy().getNode(nodeT, name)
    node = initNode(nodeT)
    return node

@reconnecting
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

    conn().proxy().getNodes(nodes, f)
    ret = [initNode(nodeT) for nodeT in nodes]
    return ret

@reconnecting
def set_node_locked(Node node, bint locked):
    """
    Set the lock state of the node 

    :param node: :class:`.Node`
    :param locked: bool 
    """
    conn().proxy().setNodeLocked(node.id, locked)
    node.node.locked = locked

@reconnecting
def set_node_cluster(Node node, Cluster cluster):
    """
    Assign the node to a cluster

    :param node: :class:`.Node`
    :param cluster: :class:`.Cluster`
    """
    conn().proxy().setNodeCluster(node.id, cluster.id)
    node.node.clusterId = cluster.id
    node.node.clusterName = cluster.name

@reconnecting
def set_node_tags(Node node, c_set[string] tags):
    """
    Set the tags for the node 

    :param node: :class:`.Node`
    :param tags: set(str) 
    """
    conn().proxy().setNodeTags(node.id, tags)
    node.node.tags = tags

@reconnecting
def set_node_slot_mode(Node node, int mode, int cores, int ram):
    """
    Set the slot mode for a node

    :param node: :class:`.Node`
    :param mode: :data:`.SlotMode`
    :param cores: int number of cores
    :param ram: int ram in MB
    """
    cdef SlotMode_type typ = <SlotMode_type>mode
    conn().proxy().setNodeSlotMode(node.id, typ, cores, ram)
    node.node.mode = typ
    node.node.slotCores = cores
    node.node.slotRam = ram


