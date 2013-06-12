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

    def get_procs(self):
        """
        Get current procs 

        :returns: list[:class:`.Proc`]
        """
        return get_procs(nodeIds=[self.id])


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




#######################
# ProcFilter
#
@cython.internal
cdef class ProcFilter:

    cdef ProcFilterT value

    def __init__(self, **kwargs):
        self.value.projectIds = kwargs.get('projectIds', [])
        self.value.folderIds = kwargs.get('folderIds', [])
        self.value.jobIds = kwargs.get('jobIds', [])
        self.value.layerIds = kwargs.get('layerIds', [])
        self.value.taskIds = kwargs.get('taskIds', [])
        self.value.clusterIds = kwargs.get('clusterIds', [])
        self.value.quotaIds = kwargs.get('quotaIds', [])
        self.value.nodeIds = kwargs.get('nodeIds', [])

        self.value.lastUpdateTime = kwargs.get('lastUpdateTime', 0)
        self.value.limit = kwargs.get('limit', 0)
        self.value.offset = kwargs.get('offset', 0)


#######################
# Node
#

cdef inline Proc initProc(ProcT& p):
    cdef Proc proc = Proc()
    proc.setProc(p)
    return proc


cdef class Proc(PlowBase):
    """
    Represents an existing Proc on a Node

    :var id: str
    :var nodeId: str
    :var jobName: str
    :var layerName: str
    :var taskName: str
    :var cores: int
    :var usedCores: float
    :var highCores: float
    :var ram: int
    :var usedRam: int
    :var highRam: int
    :var ioStats: list[long]
    :var createdTime: long msec epoch
    :var updatedTime: long msec epoch
    :var startedTime: long msec epoch

    """
    cdef ProcT proc

    cdef setProc(self, ProcT& p):
        self.proc = p

    property id:
        def __get__(self): return self.proc.id

    property nodeId:
        def __get__(self): return self.proc.nodeId

    property jobName:
        def __get__(self): return self.proc.jobName

    property layerName:
        def __get__(self): return self.proc.layerName

    property taskName:
        def __get__(self): return self.proc.taskName

    property cores:
        def __get__(self): return self.proc.cores

    property usedCores:
        def __get__(self): return self.proc.usedCores

    property highCores:
        def __get__(self): return self.proc.highCores

    property ram:
        def __get__(self): return self.proc.ram

    property usedRam:
        def __get__(self): return self.proc.usedRam

    property highRam:
        def __get__(self): return self.proc.highRam

    property ioStats:
        def __get__(self): 
            cdef:
                int i
                long val
                vector[long] ret

            for i in range(self.proc.ioStats.size()):
                val = self.proc.ioStats.at(i)
                ret.push_back(val)

            return ret

    property createdTime:
        def __get__(self): return long(self.proc.createdTime)

    property updatedTime:
        def __get__(self): return long(self.proc.updatedTime)

    property startedTime:
        def __get__(self): return long(self.proc.startedTime)        


@reconnecting
def get_proc(Guid& id):
    """
    Get a proc by id 

    :param id: str :class:`.Proc` id 
    :returns: :class:`.Proc`
    """
    cdef: 
        ProcT procT
        Proc proc

    conn().proxy().getProc(procT, id)
    proc = initProc(procT)
    return proc

@reconnecting
def get_procs(**kwargs):
    """
    Get a list of procs matching a criteria.

    :param projectIds: list[str :class:`.Project` ids]
    :param folderIds: list[str :class:`.Folder` ids]
    :param jobIds: list[str :class:`.Job` ids]
    :param layerIds: list[str :class:`.Layer` ids]
    :param taskIds: list[str :class:`.Task` ids]
    :param clusterIds: list[str :class:`.Cluster` ids]
    :param quotaIds: list[str :class:`.Quota` ids]
    :param nodeIds: list[str :class:`.Node` ids]
    :param lastUpdateTime: long msec epoch
    :param limit: int
    :param offset: int
    :returns: list[:class:`.Proc`]
    """
    cdef: 
        vector[ProcT] procs 
        ProcT procT
        list ret 
        ProcFilter filter = ProcFilter(**kwargs)
        ProcFilterT f = filter.value

    conn().proxy().getProcs(procs, f)
    ret = [initProc(procT) for procT in procs]
    return ret



