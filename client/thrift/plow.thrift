/**
* Plow Thrift RPC Interface Definition
**/
include "common.thrift"

namespace java com.breakersoft.plow.thrift
namespace py rpc
namespace cpp rpc

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
    2:string name,
    3:JobState state,
    4:bool paused
    5:i32 minCores,
    6:i32 maxCores,
    7:common.Timestamp startTime,
    8:common.Timestamp stopTime,
}

struct LayerT {
    1:common.Guid id,
    2:string name,
    3:string range,
    4:i32 chunk,
    5:set<string> tags,
    6:i32 minCores,
    7:i32 maxCores,
    8:i32 minRamMb
}

struct TaskT {
    1:common.Guid id,
    2:string name,
    3:TaskState state,
    4:i32 maxRss,
    5:i32 currentRss,
    6:i32 runTime,
    7:common.Timestamp startTime,
    8:common.Timestamp stopTime,
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
    4:i32 uid
}

struct Blueprint {
    1:JobBp job,
    2:list<LayerBp> layers
}


service RpcServiceApi {
    
    JobT launch(1:Blueprint blueprint) throws (1:PlowException e),
    JobT getActiveJob(1:string name) throws (1:PlowException e),
    JobT getJob(1:common.Guid jobId) throws (1:PlowException e),
    
    LayerT getLayer(1:common.Guid layerId) throws (1:PlowException e),
    list<LayerT> getLayers(1:common.Guid jobId) throws (1:PlowException e),

    TaskT getTask(1:common.Guid taskId) throws (1:PlowException e),
    list<TaskT> getTasks(1:common.Guid layerId) throws (1:PlowException e)
}



