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

struct RunProcessCommand {
    1:common.Guid procId,
    2:common.Guid frameId,
    3:i32 cores,
    4:list<string> command,
    5:map<string,string> env,
    6:string logFile
}

struct Process {
    1:common.Guid procId,
    2:common.Guid frameId,
    3:i64 maxRss,
    4:i32 pid
}

struct ProcessResult {
    1:Process process,
    2:i32 exitStatus,
    3:optional byte signal
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
    6:list<Process> processes;
}

service RndServiceApi {
        
    void sendPing(1:Ping ping) throws (1:RndException e),
    void processCompleted(1:ProcessResult result) throws (1:RndException e)
}

service RndNodeApi {
        
    void runProcess(1:RunProcessCommand process) throws (1:RndException ouch),
}
