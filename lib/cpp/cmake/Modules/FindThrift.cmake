# - Find Thrift (a cross platform RPC lib/tool)
# This module defines
#  Thrift_VERSION, version string of ant if found
#  Thrift_INCLUDE_DIR, where to find Thrift headers
#  Thrift_LIBS, Thrift libraries
#  Thrift_FOUND, If false, do not try to use ant


find_path(Thrift_INCLUDE_DIR Thrift.h PATHS
  /usr/local/include/thrift
  /opt/local/include/thrift
)

set(Thrift_LIB_PATHS /usr/local/lib /opt/local/lib)

find_library(Thrift_LIB NAMES thrift PATHS ${Thrift_LIB_PATHS})

if (Thrift_LIB AND Thrift_INCLUDE_DIR)
  set(Thrift_FOUND TRUE)
  set(Thrift_LIBS ${Thrift_LIB})
else ()
  set(Thrift_FOUND FALSE)
  if (NOT LibEvent_LIBS OR NOT LibEvent_INCLUDE_DIR)
    message(STATUS "libevent is required for thrift broker support")
  endif ()
endif ()

if (Thrift_FOUND)
  if (NOT Thrift_FIND_QUIETLY)
    message(STATUS "Found thrift lib: ${Thrift_LIBS}")
    message(STATUS "Found thrift include: ${Thrift_INCLUDE_DIR}")
    message(STATUS "    compiler: ${Thrift_VERSION}")
  endif ()
else ()
  message(STATUS "Thrift compiler/libraries NOT found. "
          "Thrift support will be disabled (${Thrift_RETURN}, "
          "${Thrift_INCLUDE_DIR}, ${Thrift_LIB}, ${Thrift_NB_LIB})")
endif ()

mark_as_advanced(
  Thrift_LIB
  Thrift_INCLUDE_DIR
)
