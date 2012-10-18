
include_directories(
    ${CMAKE_SOURCE_DIR}/export
    ${Thrift_INCLUDE_DIR}
    ${Thrift_INCLUDE_DIR}/thrift
    ${Boost_INCLUDE_DIRS}
)

file(GLOB_RECURSE src_files "${CMAKE_SOURCE_DIR}/core/*.cpp")
file(GLOB_RECURSE core_export_headers "${CMAKE_SOURCE_DIR}/export/plow/*.h")

message(STATUS "Create PlowABI.h from PlowABI.h.in")
configure_file(${CMAKE_SOURCE_DIR}/export/plow/plowABI.h.in
    ${CMAKE_BINARY_DIR}/export/plow/plowABI.h @ONLY)
list(APPEND core_export_headers ${CMAKE_BINARY_DIR}/export/plowABI.h)

add_library(plow SHARED ${src_files})
add_dependencies(plow Thrift)
target_link_libraries(plow Thrift)
