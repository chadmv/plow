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
    2:string name
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
    2:i32 uid,
    3:list<LayerBp> layers
}

struct Blueprint {
    1:JobBp job,
    2:string project,
    3:bool paused,
}


service RpcServiceApi {
    
    JobT launch(1:Blueprint blueprint) throws (1:PlowException e),
    
}



