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
    REPAIR
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

struct TaskTotalsT {
    1:i32 totalTaskCount = 0,
    2:i32 succeededTaskCount = 0,
    3:i32 runningTaskCount = 0,
    4:i32 deadTaskCount = 0,
    5:i32 eatenTaskCount = 0,
    6:i32 waitingTaskCount = 0,
    7:i32 dependTaskCount = 0,
}

struct ProjectT {
    1:common.Guid id,
    2:string name,
    3:string title
}

struct ClusterCountsT {
    1:required i32 nodes,
    2:required i32 upNodes, 
    3:required i32 downNodes,
    4:required i32 repairNodes,
    5:required i32 lockedNodes,
    6:required i32 unlockedNodes,
    7:required i32 cores,
    8:required i32 upCores, 
    9:required i32 downCores,
    10:required i32 repairCores,
    11:required i32 lockedCores,
    12:required i32 unlockedCores,
    13:required i32 runCores,
    14:required i32 idleCores
}

struct ClusterT {
    1:required common.Guid id,
    2:string name,
    3:set<string> tags,
    4:bool isLocked,
    5:bool isDefault,
    6:ClusterCountsT total
}

struct QuotaT {
    1:common.Guid id,
    2:common.Guid clusterId,
    3:common.Guid projectId,
    4:string name,
    5:bool isLocked,
    6:i32 size,
    7:i32 burst,
    8:i32 runCores
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
    6:set<string> tags,
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
    10:i32 runCores,
    11:common.Timestamp startTime,
    12:common.Timestamp stopTime,
    13:TaskTotalsT totals,
    14:i32 maxRssMb
}

struct LayerT {
    1:common.Guid id,
    2:string name,
    3:string range,
    4:i32 chunk,
    5:set<string> tags,
    6:bool threadable
    7:i32 minCores,
    8:i32 maxCores,
    9:i32 minRamMb,
    10:i32 runCores,
    11:TaskTotalsT totals,
    12:i32 maxRssMb,
    13:i16 maxCpuPerc
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
    10:string lastLogLine,
    11:i32 retries,
    12:i32 cores,
    13:i32 ramMb,
    14:i32 rssMb,
    15:i32 maxRssMb,
    16:i16 cpuPerc,
    17:i16 maxCpuPerc,
    18:i32 progress
}

struct FolderT {
    1:common.Guid id,
    2:string name,
    3:i32 minCores,
    4:i32 maxCores,
    5:i32 runCores,
    6:i32 order,
    7:TaskTotalsT totals,
    8:optional list<JobT> jobs
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

struct TaskSpecT {
    1:string name,
    2:list<DependSpecT> depends
}

struct LayerSpecT {
    1:string name,
    2:list<string> command,
    3:set<string> tags,
    4:optional string range,
    5:i32 chunk = 1,
    6:i32 minCores = 1,
    7:i32 maxCores = 1,
    8:i32 minRamMb = 1024,
    9:bool threadable = false,
    10:list<DependSpecT> depends,
    11:list<TaskSpecT> tasks
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
    1:bool matchingOnly = false,
    2:optional list<string> project,
    3:optional list<string> user,
    4:optional string regex,
    5:optional list<JobState> states,
    6:optional list<common.Guid> jobIds,
    7:optional list<string> name
}

struct TaskFilterT {
    1:common.Guid jobId,
    2:list<common.Guid> layerIds,
    3:list<TaskState> states,
    4:i32 limit = 0,
    5:i32 offset = 0,
    6:i64 lastUpdateTime = 0,
    7:list<common.Guid> taskIds
}

struct NodeFilterT {
    1:list<common.Guid> hostIds,
    2:list<common.Guid> clusterIds,
    3:string regex,
    4:list<string> hostnames,
    5:list<NodeState> states,
    6:list<LockState> lockStates
}

struct QuotaFilterT {
    1:optional list<common.Guid> project,
    2:optional list<common.Guid> cluster
}

struct OutputT {
    1:string path,
    2:common.Attrs attrs
}

service RpcService {
    
    i64 getPlowTime() throws (1:PlowException e),

    ProjectT getProject(1:common.Guid id) throws (1:PlowException e),
    ProjectT getProjectByName(1:string name) throws (1:PlowException e),

    list<ProjectT> getProjects() throws (1:PlowException e),

    JobT launch(1:JobSpecT spec) throws (1:PlowException e),
    JobT getActiveJob(1:string name) throws (1:PlowException e),
    JobT getJob(1:common.Guid jobId) throws (1:PlowException e),
    bool killJob(1:common.Guid jobId, 2:string reason) throws (1:PlowException e),
    void pauseJob(1:common.Guid jobId, 2:bool paused) throws (1:PlowException e),
    list<JobT> getJobs(1:JobFilterT filter) throws (1:PlowException e),
    list<OutputT> getJobOutputs(1:common.Guid jobId) throws (1:PlowException e),

    FolderT createFolder(1:string projectId, 2:string name) throws (1:PlowException e),
    FolderT getFolder(1:string id) throws (1:PlowException e),
    list<FolderT> getJobBoard(1:common.Guid project) throws (1:PlowException e),
    list<FolderT> getFolders(1:common.Guid project) throws (1:PlowException e),
    LayerT getLayerById(1:common.Guid layerId) throws (1:PlowException e),
    LayerT getLayer(1:common.Guid jobId, 2:string name) throws (1:PlowException e),

    list<LayerT> getLayers(1:common.Guid jobId) throws (1:PlowException e),
    void addOutput(1:common.Guid layerId, 2:string path, 3:common.Attrs attrs) throws (1:PlowException e)
    list<OutputT> getLayerOutputs(1:common.Guid layerId) throws (1:PlowException e),
    void setLayerTags(1:common.Guid guid, 2:set<string> tags) throws (1:PlowException e),
    void setLayerMinCoresPerTask(1:common.Guid guid, 2:i32 minCores) throws (1:PlowException e),
    void setLayerMaxCoresPerTask(1:common.Guid guid, 2:i32 minCores) throws (1:PlowException e),
    void setLayerMinRamPerTask(1:common.Guid guid, 2:i32 minCores) throws (1:PlowException e),
    void setLayerThreadable(1:common.Guid guid, 2:bool threadable) throws (1:PlowException e),

    TaskT getTask(1:common.Guid taskId) throws (1:PlowException e),
    list<TaskT> getTasks(1:TaskFilterT filter) throws (1:PlowException e),
    string getTaskLogPath(1:common.Guid taskId) throws (1:PlowException e),
    void retryTasks(1:TaskFilterT filter) throws (1:PlowException e),
    void eatTasks(1:TaskFilterT filter) throws (1:PlowException e),
    void killTasks(1:TaskFilterT filter) throws (1:PlowException e),

    NodeT getNode(1:string name) throws (1:PlowException e),
    list<NodeT> getNodes(1:NodeFilterT filter) throws (1:PlowException e),

    ClusterT getCluster(1:string name) throws (1:PlowException e),
    list<ClusterT> getClustersByTag(1:string tag) throws (1:PlowException e),
    list<ClusterT> getClusters() throws (1:PlowException e),
    ClusterT createCluster(1:string name, 2:set<string> tags) throws (1:PlowException e),
    bool deleteCluster(1:common.Guid id) throws (1:PlowException e),
    bool lockCluster(1:common.Guid id, 2:bool locked) throws (1:PlowException e),
    void setClusterTags(1:common.Guid id, 2:set<string> tags) throws (1:PlowException e),
    void setClusterName(1:common.Guid id, 2: string name) throws (1:PlowException e),
    void setDefaultCluster(1:common.Guid id) throws (1:PlowException e),

    QuotaT getQuota(1:common.Guid id) throws (1:PlowException e),
    list<QuotaT> getQuotas(1:QuotaFilterT filter) throws (1:PlowException e),
    QuotaT createQuota(1:common.Guid projectId, 2:common.Guid clusterId, 3:i32 size, 4:i32 burst) throws (1:PlowException e),
    void setQuotaSize(1:common.Guid id, 2:i32 size) throws (1:PlowException e),
    void setQuotaBurst(1:common.Guid id, 2:i32 burst) throws (1:PlowException e),
    void setQuotaLocked(1:common.Guid id, 2:bool locked) throws (1:PlowException e)
}
