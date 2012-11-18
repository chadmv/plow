
include_directories(
    ${Thrift_INCLUDE_DIR}
    # Necessary for thrift generated code.
    ${CMAKE_SOURCE_DIR}/src/export/plow
)

file(GLOB_RECURSE src_files "${CMAKE_SOURCE_DIR}/src/core/*.cpp")
#file(GLOB_RECURSE core_export_headers "${CMAKE_SOURCE_DIR}/src/export/plow/*.h")

message(STATUS "Create plow_abi.h from plow_abi.h.in")
configure_file(${CMAKE_SOURCE_DIR}/src/export/plow/plow_abi.h.in
    ${CMAKE_BINARY_DIR}/src/export/plow/plow_abi.h @ONLY)
list(APPEND core_export_headers ${CMAKE_BINARY_DIR}/src/export/plow_abi.h)

add_library(plow SHARED ${src_files})
add_dependencies(plow Thrift)
target_link_libraries(plow Thrift ${Boost_THREAD_LIBRARY})
