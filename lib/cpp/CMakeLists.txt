project(Plow)
cmake_minimum_required(VERSION 2.8)

set(CMAKE_OSX_ARCHITECTURES "x86_64")

set (PLOW_VERSION_MAJOR 1)
set (PLOW_VERSION_MINOR 0)
set (PLOW_VERSION_PATCH 0)

set(SOVERSION ${PLOW_VERSION_MAJOR})
set(PLOW_VERSION "${PLOW_VERSION_MAJOR}.${PLOW_VERSION_MINOR}.${PLOW_VERSION_PATCH}")
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")

enable_language(CXX)
set(PLOW_NAMESPACE Plow)

find_package(Boost COMPONENTS REQUIRED thread program_options)
find_package(Thrift REQUIRED)
find_package(Qt4 REQUIRED)

SUBDIRS(src)
