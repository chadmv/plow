

PROJECT(PLOW_TOOL)

INCLUDE_DIRECTORIES(${CMAKE_CURRENT_BINARY_DIR})
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/src/export/plow)
INCLUDE_DIRECTORIES(${CMAKE_SOURCE_DIR}/src/tools/spade)
INCLUDE_DIRECTORIES(${Boost_INCLUDE_DIR})

add_executable(spade
    output.cpp
    main.cpp
)

target_link_libraries(spade plow Thrift ${Boost_LIBRARIES})
