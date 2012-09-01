/**
* Plow Thrift RPC Interface Definition
**/

namespace java com.breakersoft.plow.thrift
namespace py rpc

/**
* API version.
*
*  - Major: Incremented for backward incompatible changes.
*  - Minor: Incremented for backard compatible changes.
*  - Patch: Incremented for bug fixes.
* See the Semantic Versioning Specification (SemVer) http://semver.org
**/
const string VERSION = "0.1.0"

/**
* A GUID type. Every object has a unique GUI in the format
* [a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$
**/
typedef string Guid

/**
* Epoch time in milliseconds.
**/
typedef i64 Timestamp

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
* Frame State Enum
**/
enum FrameState {
    WAITING,
    RUNNING,
    DEAD,
    EATEN,
    DEPEND,
    SUCCEEDED
}