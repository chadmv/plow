cmake_minimum_required(VERSION 2.8)

set(CMAKE_OSX_ARCHITECTURES "x86_64")
project(PLOW_CYTHON)

# Versions
set (PLOW_CYTHON_VERSION_MAJOR 0)
set (PLOW_CYTHON_VERSION_MINOR 1)
set (PLOW_CYTHON_VERSION_PATCH 0)

set(SOVERSION ${PLOW_CYTHON_VERSION_MAJOR})
set(PLOW_CYTHON_VERSION "${PLOW_CYTHON_VERSION_MAJOR}.${PLOW_CYTHON_VERSION_MINOR}.${PLOW_CYTHON_VERSION_PATCH}")



set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")

find_package (PythonLibs 2.6 REQUIRED)
find_package (Boost COMPONENTS REQUIRED thread)
find_package (Thrift REQUIRED)

include( UseCython )

add_custom_target( ReplicatePythonSourceTree ALL ${CMAKE_COMMAND} -P
  ${CMAKE_CURRENT_SOURCE_DIR}/cmake/Modules/ReplicatePythonSourceTree.cmake
  ${CMAKE_CURRENT_BINARY_DIR}
  WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR} )


# Process the CMakeLists.txt in the 'src' and 'bin' directory.
add_subdirectory( src )
