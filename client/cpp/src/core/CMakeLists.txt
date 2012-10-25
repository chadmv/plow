
include_directories(
    ${CMAKE_SOURCE_DIR}/src/export
    ${Thrift_INCLUDE_DIR}
    ${Thrift_INCLUDE_DIR}/thrift
    ${Boost_INCLUDE_DIRS}
)

file(GLOB_RECURSE src_files "${CMAKE_SOURCE_DIR}/src/core/*.cpp")
file(GLOB_RECURSE core_export_headers "${CMAKE_SOURCE_DIR}/src/export/plow/*.h")

message(STATUS "Create PlowABI.h from PlowABI.h.in")
configure_file(${CMAKE_SOURCE_DIR}/src/export/plow/plowABI.h.in
    ${CMAKE_BINARY_DIR}/src/export/plow/plowABI.h @ONLY)
list(APPEND core_export_headers ${CMAKE_BINARY_DIR}/src/export/plowABI.h)

add_library(plow SHARED ${src_files})
add_dependencies(plow Thrift)
target_link_libraries(plow Thrift)
