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
    4:common.Guid layerId,
    5:i32 cores,
    6:list<string> command,
    7:map<string,string> env,
    8:string logFile,
    9:i32 uid,
    10:string username,
    11:optional list<string> taskTypes
}

struct RunningTask {
    1:common.Guid procId,
    2:common.Guid taskId,
    3:common.Guid jobId,
    4:common.Guid layerId,
    5:i32 rssMb,
    6:i32 pid,
    7:optional double progress = 0.0,
    8:optional string lastLog = "",
    9:i16 cpuPercent
}

struct RunTaskResult {
    1:common.Guid procId,
    2:common.Guid taskId,
    3:common.Guid jobId,
    4:i32 maxRssMb,
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
    8:string platform,
    9:list<double> load
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
        
    oneway void sendPing(1:Ping ping),
    void taskComplete(1:RunTaskResult result) throws (1:RndException e)
}

service RndNodeApi {
        
    void runTask(1:RunTaskCommand command) throws (1:RndException e),
    void killRunningTask(1:common.Guid procId, 2:string reason) throws (1:RndException e),
    list<RunningTask> getRunningTasks() throws (1:RndException e),

    void reboot(1: bool now) throws (1:RndException e),
}
