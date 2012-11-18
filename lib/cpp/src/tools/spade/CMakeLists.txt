PROJECT(PLOW_TOOL)

file(GLOB_RECURSE spade_src_files "${CMAKE_SOURCE_DIR}/src/tools/spade/*.cpp")

add_executable(spade ${spade_src_files})
target_link_libraries(spade plow Thrift ${Boost_LIBRARIES})
