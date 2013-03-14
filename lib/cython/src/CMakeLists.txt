
PROJECT(PLOW_CLIENT)

include_directories(
    /usr/include
    /usr/local/include
    ${CMAKE_SOURCE_DIR}/src/export
    ${CMAKE_SOURCE_DIR}/src
    ${Boost_INCLUDE_DIRS}
)

SUBDIRS(core)