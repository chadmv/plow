/**
* Render Node Daemon Thrift Interface Definition
**/

include "common.thrift"

namespace java com.breakersoft.plow.rnd.thrift
namespace py rpc
namespace cpp rpc

exception RndException {
  1: i32 what,
  2: string why,
  3: optional list<string> stack
}

struct RunTaskCommand {
    1:common.Guid procId,
    2:common.Guid taskId,
    3:common.Guid jobId,
    4:i32 cores,
    5:list<string> command,
    6:map<string,string> env,
    7:string logFile
}

struct RunningTask {
    1:common.Guid procId,
    2:common.Guid taskId,
    3:common.Guid jobId,
    4:i64 maxRss,
    5:i32 pid
}

struct RunTaskResult {
    1:common.Guid procId,
    2:common.Guid taskId,
    3:common.Guid jobId,
    4:i64 maxRss,
    5:i32 exitStatus,
    6:optional byte exitSignal
}

struct Hardware {
    1:i16 physicalCpus,
    2:i16 logicalCpus
    3:i32 totalRamMb
    4:i32 freeRamMb,
    5:i32 totalSwapMb,
    6:i32 freeSwapMb,
    7:string cpuModel,
    8:string platform
}

struct Ping {
    1:string hostname,
    2:string ipAddr,
    3:bool isReboot,
    4:i64 bootTime,
    5:Hardware hw,
    6:list<RunningTask> tasks;
}

service RndServiceApi {
        
    void sendPing(1:Ping ping) throws (1:RndException e),
    void taskComplete(1:RunTaskResult result) throws (1:RndException e)
}

service RndNodeApi {
        
    void runTask(1:RunTaskCommand command) throws (1:RndException ouch),
}
