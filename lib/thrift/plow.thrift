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
    8:i32 minRamMb
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

service RpcService {
    
    JobT launch(1:JobSpecT spec) throws (1:PlowException e),
    JobT getActiveJob(1:string name) throws (1:PlowException e),
    JobT getJob(1:common.Guid jobId) throws (1:PlowException e),
    bool killJob(1:common.Guid jobId, 2:string reason) throws (1:PlowException e),

    list<JobT> getJobs(1:JobFilterT filter) throws (1:PlowException e),

    LayerT getLayer(1:common.Guid layerId) throws (1:PlowException e),
    list<LayerT> getLayers(1:common.Guid jobId) throws (1:PlowException e),

    TaskT getTask(1:common.Guid taskId) throws (1:PlowException e),
    list<TaskT> getTasksByLayer(1:common.Guid layerId, 2:list<i32> limit) throws (1:PlowException e),
    list<TaskT> getTasksByJob(1:common.Guid jobId, 2:list<i32> limit) throws (1:PlowException e),

}



