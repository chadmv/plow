from libcpp.string cimport string
from libcpp.vector cimport vector
from libcpp.set cimport set
from libcpp.map cimport map 



cdef extern from "rpc/common_types.h" namespace "Plow":
    ctypedef string Guid
    ctypedef int Timestamp
    ctypedef map[string, string] Attrs



cdef extern from "rpc/plow_types.h" namespace "Plow":

    ctypedef enum JobState_type "Plow::JobState::type":
        JOBSTATE_INITIALIZE "Plow::JobState::INITIALIZE"
        JOBSTATE_RUNNING "Plow::JobState::RUNNING"
        JOBSTATE_FINISHED "Plow::JobState::FINISHED" 

    ctypedef enum TaskState_type "Plow::TaskState::type":
        TASKSTATE_INITIALIZE "Plow::TaskState::INITIALIZE"
        TASKSTATE_WAITING "Plow::TaskState::WAITING" 
        TASKSTATE_RUNNING "Plow::TaskState::RUNNING"
        TASKSTATE_DEAD "Plow::TaskState::DEAD"
        TASKSTATE_EATEN "Plow::TaskState::EATEN"
        TASKSTATE_DEPEND "Plow::TaskState::DEPEND"
        TASKSTATE_SUCCEEDED "Plow::TaskState::SUCCEEDED"

    ctypedef enum NodeState_type "Plow::NodeState::type":
        NODESTATE_INITIALIZE "Plow::NodeState::UP"
        NODESTATE_RUNNING "Plow::NodeState::DOWN"
        NODESTATE_FINISHED "Plow::NodeState::REPAIR" 

    ctypedef enum LockState_type "Plow::LockState::type":
        LOCKSTATE_OPEN "Plow::LockState::OPEN"
        LOCKSTATE_LOCKED "Plow::LockState::LOCKED"

    ctypedef enum DependType_type "Plow::DependType::type":
        JOB_ON_JOB"Plow::DependType::JOB_ON_JOB"
        LAYER_ON_LAYER "Plow::DependType::LAYER_ON_LAYER"
        LAYER_ON_TASK "Plow::DependType::LAYER_ON_TASK"
        TASK_ON_LAYER "Plow::DependType::TASK_ON_LAYER"
        TASK_ON_TASK "Plow::DependType::TASK_ON_TASK"
        TASK_BY_TASK "Plow::DependType::TASK_BY_TASK"


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
        JobState_type state
        LockState_type lockState
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
        JobState_type state
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
        TaskState_type state
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
        DependType_type type
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
        bint matchingOnly
        vector[string] project 
        vector[string] user 
        string regex 
        vector[JobState_type] states 
        vector[Guid] jobIds
        vector[string] name 

    cdef cppclass TaskFilterT:
        Guid jobId 
        vector[Guid] layerIds
        vector[TaskState_type] states 
        vector[Guid] taskIds
        int limit 
        int offset 
        int lastUpdateTime

    cdef cppclass NodeFilterT:
        vector[Guid] hostIds
        vector[Guid] clusterIds
        string regex
        vector[string] hostnames
        vector[NodeState_type] states
        vector[LockState_type] lockStates

    cdef cppclass OutputT:
        string path 
        Attrs attrs



