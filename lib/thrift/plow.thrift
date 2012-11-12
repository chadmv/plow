/**
* Plow Thrift RPC Interface Definition
**/
include "common.thrift"

namespace java com.breakersoft.plow.thrift
namespace py rpc
namespace cpp Plow

/**
* Job State Enum
**/
enum JobState {
    // Job is in the process of launching.
    INITIALIZE,
    // Job is now accepting procs.
    RUNNING,
    // The job has been stopped.
    FINISHED
}

/**
* Task State Enum
**/
enum TaskState {
    INITIALIZE,
    WAITING,
    RUNNING,
    DEAD,
    EATEN,
    DEPEND,
    SUCCEEDED
}

/**
* Node state enumeration
**/
enum NodeState {
    UP,
    DOWN,
    REPAIR,    
    REBOOT
}

enum LockState {
    OPEN,
    LOCKED
}

enum DependType {
    JOB_ON_JOB,
    LAYER_ON_LAYER,
    LAYER_ON_TASK,
    TASK_ON_LAYER,
    TASK_ON_TASK,
    TASK_BY_TASK
}

exception PlowException {
    1: i32 what,
    2: string why
}

struct ProjectT {
    1:common.Guid id,
    2:string code,
    3:string title
}

struct ClusterT {
    1:common.Guid id,
    2:string name,
    3:string tag,
    4:bool locked,
    5:bool defaultCluster,
    6:i32 totalHosts,
    7:i32 downHosts,
    8:i32 repairHosts,
    9:i32 rebootHosts,
    10:i32 totalCores,
    11:i32 runCores,
    12:i32 idleCores
}

struct QuotaT {
    1:common.Guid id,
    2:common.Guid clusterId,
    3:common.Guid projectId,
    4:string clusterName,
    5:string projectName
    6:bool locked,
    7:i32 totalCores,
    8:i32 burstCores,
    9:i32 freeCores,
}

struct NodeSystemT {
    1:i32 physicalCores,
    2:i32 logicalCores,
    3:i32 totalRamMb,               
    4:i32 freeRamMb,
    5:i32 totalSwapMb,
    6:i32 freeSwapMb,
    7:string cpuModel,
    8:string platform,
    9:list<i32> load
}

struct NodeT {
    1:common.Guid id,
    2:common.Guid clusterId,
    3:string name,
    4:string clusterName,
    5:string ipaddr,
    6:list<string> tags,
    7:NodeState state,
    8:LockState lockState,
    9:common.Timestamp createdTime,
    10:common.Timestamp updatedTime,
    11:common.Timestamp bootTime,
    12:i32 totalCores,
    13:i32 idleCores,
    14:i32 totalRamMb,
    15:i32 freeRamMb,
    16:NodeSystemT system
}

struct ProcT {
    1:common.Guid id,
    2:common.Guid hostId,
    3:string jobName,
    4:string taskName,
    5:i32 cores,
    6:i32 ramMb,
    7:i32 usedRamMb,
    8:i32 highRamMb,
    9:bool unbooked
}

struct JobT {
    1:common.Guid id,
    2:common.Guid folderId,
    3:string name,
    4:string username,
    5:i32 uid,
    6:JobState state,
    7:bool paused
    8:i32 minCores,
    9:i32 maxCores,
    10:common.Timestamp startTime,
    11:common.Timestamp stopTime,
    12:i32 totalTaskCount,
    13:i32 succeededTaskCount,
    14:i32 runningTaskCount,
    15:i32 deadTaskCount,
    16:i32 eatenTaskCount,
    17:i32 waitingTaskCount,
    18:i32 dependTaskCount,
    19:i32 runningCoreCount,
}

struct LayerT {
    1:common.Guid id,
    2:string name,
    3:string range,
    4:i32 chunk,
    5:set<string> tags,
    6:i32 minCores,
    7:i32 maxCores,
    8:i32 minRamMb,
    9:i32 totalTaskCount,
    10:i32 succeededTaskCount,
    11:i32 runningTaskCount,
    12:i32 deadTaskCount,
    13:i32 eatenTaskCount,
    14:i32 waitingTaskCount,
    15:i32 dependTaskCount,
    16:i32 runningCoreCount,
}

struct TaskT {
    1:common.Guid id,
    2:string name,
    3:i32 number,
    4:i32 dependCount,
    5:i32 order,
    6:TaskState state,
    7:common.Timestamp startTime,
    8:common.Timestamp stopTime,
    9:string lastNodeName,
    10:i32 lastRss,
    11:i32 lastCores,
    12:i32 lastMaxRss
}

/**
* DependSpecT describes a dependency launched with a JobSpec.
**/
struct DependSpecT {
    1:DependType type,
    2:string dependentJob,
    3:string dependOnJob,
    4:string dependentLayer,
    5:string dependOnLayer,
    6:string dependentTask,
    7:string dependOnTask
}

struct LayerSpecT {
    1:string name,
    2:list<string> command;
    3:set<string> tags,
    4:string range,
    5:i32 chunk,
    6:i32 minCores,
    7:i32 maxCores,
    8:i32 minRamMb,
    9:list<DependSpecT> depends
}

struct JobSpecT {
    1:string name,
    2:string project,
    3:bool paused,
    4:string username,
    5:i32 uid,
    6:string logPath
    7:list<LayerSpecT> layers,
    8:list<DependSpecT> depends
}

struct JobFilterT {
    1:list<string> project,
    2:list<string> user,
    3:string regex,
    4:list<JobState> states
}

struct TaskFilterT {
    1:required common.Guid jobId,
    2:list<common.Guid> layerIds,
    3:list<TaskState> states,
    4:i32 limit = 0,
    5:i32 offset = 0
}

struct NodeFilterT {
    1:list<common.Guid> hostIds,
    2:list<common.Guid> clusterIds,
    3:string regex,
    4:list<string> hostnames,
    5:list<NodeState> states,
    6:list<LockState> lockStates
}

service RpcService {
    
    JobT launch(1:JobSpecT spec) throws (1:PlowException e),
    JobT getActiveJob(1:string name) throws (1:PlowException e),
    JobT getJob(1:common.Guid jobId) throws (1:PlowException e),
    bool killJob(1:common.Guid jobId, 2:string reason) throws (1:PlowException e),

    list<JobT> getJobs(1:JobFilterT filter) throws (1:PlowException e),

    LayerT getLayer(1:common.Guid layerId) throws (1:PlowException e),
    list<LayerT> getLayers(1:common.Guid jobId) throws (1:PlowException e),

    TaskT getTask(1:common.Guid taskId) throws (1:PlowException e),
    list<TaskT> getTasks(1:TaskFilterT filter) throws (1:PlowException e),

    NodeT getNode(1:common.Guid nodeId) throws (1:PlowException e),
    list<NodeT> getNodes(1:NodeFilterT filter) throws (1:PlowException e)
}



