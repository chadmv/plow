/* Common Thrift Definitions */

namespace java com.breakersoft.plow.thrift
namespace py common
namespace cpp Plow

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
* A map for attrs.
**/
typedef map<string,string> Attrs


