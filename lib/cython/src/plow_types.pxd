from libcpp.set cimport set
from libcpp.string cimport string
from libcpp.vector cimport vector

from common_types cimport *

cdef extern from "rpc/plow_types.h" namespace "Plow":

    ctypedef enum enum_type:
        pass

    ctypedef enum JobState_type:
        pass

    struct JobState:
        JobState_type type

    ctypedef enum TaskState_type:
        pass

    struct TaskState:
        TaskState_type type

    ctypedef enum NodeState_type:
        pass

    struct NodeState:
        NodeState_type type

    ctypedef enum LockState_type:
        pass

    struct LockState:
        LockState_type type

    ctypedef enum DependType_type:
        pass

    struct DependType:
        DependType_type type        

    cdef cppclass PlowException:
        int what
        string why

    cdef cppclass TaskTotalsT:
        int totalTaskCount
        int succeededTaskCount
        int runningTaskCount
        int deadTaskCount
        int eatenTaskCount
        int waitingTaskCount
        int dependTaskCount

    cdef cppclass ProjectT:
        Guid id
        string name
        string title

    cdef cppclass ClusterCountsT:
        int nodes
        int upNodes
        int downNodes
        int repairNodes
        int lockedNodes
        int unlockedNodes
        int cores
        int upCores
        int downCores
        int repairCores
        int lockedCores
        int unlockedCores
        int runCores
        int idleCores                

    cdef cppclass ClusterT:
        Guid id
        string name
        set[string] tags
        bint isLocked
        bint isDefault
        ClusterCountsT total

    cdef cppclass QuotaT:
        Guid id
        Guid clusterId
        Guid projectId
        string clusterName
        string projectName
        bint locked
        int totalCores
        int burstCores
        int freeCores

    cdef cppclass NodeSystemT:
        int physicalCores
        int logicalCores
        int totalRamMb
        int freeRamMb
        int totalSwapMb
        int freeSwapMb
        string cpuModel
        string platform
        vector[int] load        

    cdef cppclass NodeT:
        Guid id
        Guid clusterId
        string name
        string clusterName
        string ipaddr
        set[string] tags
        enum_type state
        enum_type lockState
        Timestamp createdTime
        Timestamp updatedTime
        Timestamp bootTime
        int totalCores
        int idleCores
        int totalRamMb
        int freeRamMb
        NodeSystemT system

    cdef cppclass ProcT:
        Guid id
        Guid hostId
        string jobName
        string taskName
        int cores
        int ramMb
        int usedRamMb
        int highRamMb
        bint unbooked

    cdef cppclass JobT:
        Guid id
        Guid folderId
        string name
        string username
        int uid
        enum_type state
        bint paused
        int minCores
        int maxCores
        int runCores
        Timestamp startTime
        Timestamp stopTime
        TaskTotalsT totals
        int maxRssMb

    cdef cppclass LayerT:
        Guid id
        string name
        string range
        int chunk
        set[string] tags
        bint threadable
        int minCores
        int maxCores
        int minRamMb
        int runCores
        TaskTotalsT totals
        int maxRssMb
        int maxCpuPerc

    cdef cppclass TaskT:
        Guid id
        string name
        int number
        int dependCount
        int order
        enum_type state
        int startTime
        int stopTime
        string lastNodeName
        string lastLogLine
        int retries
        int cores
        int ramMb
        int rssMb
        int maxRssMb
        int cpuPerc
        int maxCpuPerc
        int progress

    cdef cppclass FolderT:
        Guid id
        string name
        int minCores
        int maxCores
        int runCores
        int order
        TaskTotalsT totals
        vector[JobT] jobs

    cdef cppclass DependSpecT:
        enum_type type
        string dependentJob
        string dependOnJob
        string dependentLayer
        string dependOnLayer
        string dependentTask
        string dependOnTask

    cdef cppclass TaskSpecT:
        string name 
        vector[DependSpecT] depends 

    cdef cppclass LayerSpecT:
        string name
        vector[string] command
        set[string] tags
        string range
        int chunk
        int minCores
        int maxCores
        int minRamMb
        bint threadable
        vector[DependSpecT] depends
        vector[TaskSpecT] tasks

    cdef cppclass JobSpecT:
        string name
        string project
        bint paused
        string username
        int uid
        string logPath
        vector[LayerSpecT] layers
        vector[DependSpecT] depends

    cdef cppclass JobFilterT:
        vector[string] project 
        vector[string] user 
        string regex 
        vector[enum_type] states 

    cdef cppclass TaskFilterT:
        Guid jobId 
        vector[Guid] layerIds
        vector[enum_type] states 
        int limit 
        int offset 
        int lastUpdateTime

    cdef cppclass NodeFilterT:
        vector[Guid] hostIds
        vector[Guid] clusterIds
        string regex
        vector[string] hostnames
        vector[enum_type] states
        vector[enum_type] lockStates

    cdef cppclass OutputT:
        string path 
        Attrs attrs
