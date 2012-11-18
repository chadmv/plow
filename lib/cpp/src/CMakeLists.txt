
PROJECT(PLOW_CLIENT)

include_directories(
    ${CMAKE_SOURCE_DIR}/src/export
    ${CMAKE_SOURCE_DIR}/src
    ${Boost_INCLUDE_DIRS}
)

SUBDIRS(core)
SUBDIRS(gui)
SUBDIRS(tools/wrangleplow)
SUBDIRS(tools/spade)