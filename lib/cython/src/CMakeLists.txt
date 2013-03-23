
include_directories( 
	${Thrift_INCLUDE_DIR}
	${Boost_INCLUDE_DIRS}
	${CMAKE_CURRENT_SOURCE_DIR}
	${CMAKE_CURRENT_SOURCE_DIR}/core 
	${CMAKE_CURRENT_SOURCE_DIR}/core/rpc 
)

file(GLOB_RECURSE src_files "${CMAKE_CURRENT_SOURCE_DIR}/core/*.cpp")


# If the pyx file is a C++ file, we should specify that here.
set_source_files_properties(
  ${CMAKE_CURRENT_SOURCE_DIR}/plow.pyx
  PROPERTIES CYTHON_IS_CXX TRUE )

# Multi-file cython modules do not appear to be working at the moment.
cython_add_module( plow plow.pyx ${src_files} )

add_dependencies(plow Thrift)
target_link_libraries(plow Thrift ${Boost_THREAD_LIBRARY})
