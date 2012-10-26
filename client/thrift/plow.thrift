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

struct LayerBp {
    1:string name,
    2:list<string> command;
    3:set<string> tags,
    4:string range,
    5:i32 chunk,
    6:i32 minCores,
    7:i32 maxCores,
    8:i32 minRamMb
}

struct JobBp {
    1:string name,
    2:string project,
    3:bool paused,
    4:string username,
    5:i32 uid
}

struct Blueprint {
    1:JobBp job,
    2:list<LayerBp> layers
}

struct JobFilter {
    1:optional list<string> project,
    2:optional list<string> user,
    3:optional string regex,
    4:optional list<JobState> states
}

service RpcServiceApi {
    
    JobT launch(1:Blueprint blueprint) throws (1:PlowException e),
    JobT getRunningJob(1:string name) throws (1:PlowException e),
    JobT getJob(1:common.Guid jobId) throws (1:PlowException e),
    list<JobT> getJobs(1:JobFilter filter) throws (1:PlowException e),

    LayerT getLayer(1:common.Guid layerId) throws (1:PlowException e),
    list<LayerT> getLayers(1:common.Guid jobId) throws (1:PlowException e),

    TaskT getTask(1:common.Guid taskId) throws (1:PlowException e),
    list<TaskT> getTasks(1:common.Guid layerId) throws (1:PlowException e)
}



