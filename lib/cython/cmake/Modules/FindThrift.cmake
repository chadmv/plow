# Find the Thrift includes and library
#
#  set THRIFT_FIND_REQUIRED to require the Thrift library
#  otherwise, the user will be given a WITH_THRIFT option.
# 
#  THRIFT_INCLUDES     - all include dirs needed for Thrift
#  THRIFT_LIBRARY      - where to find the Thrift library
#  THRIFT_LIBRARIES    - all libraries needed for Thrift
#  THRIFT_ENABLED      - true if Thrift is enabled

INCLUDE(LintelFind)

INCLUDE(FindPkgConfig)

PKG_CHECK_MODULES(THRIFT thrift)

SET(THRIFT_INCLUDES ${THRIFT_INCLUDE_DIRS})

IF(THRIFT_FOUND AND NOT EXISTS "/${THRIFT_LIBRARIES}")
    MESSAGE("Translating ${THRIFT_LIBRARIES} library to full path")
    IF("${THRIFT_LIBRARY_DIRS}" STREQUAL "") 
        SET(THRIFT_LIBRARY_DIRS "/usr/lib;/usr/lib64")
    ENDIF("${THRIFT_LIBRARY_DIRS}" STREQUAL "") 

    SET(tmp ${THRIFT_LIBRARIES})
    SET(THRIFT_LIBRARIES THRIFT-NOTFOUND)

    # get full path to library, avoid cmake error
    FIND_LIBRARY(THRIFT_LIBRARIES NAMES ${tmp}
      PATHS ${THRIFT_LIBRARY_DIRS} NO_DEFAULT_PATH)

    IF(NOT THRIFT_LIBRARIES)
        MESSAGE(FATAL_ERROR "Internal, can't find thrift library under ${THRIFT_LIBRARY_DIRS}")
    ENDIF(NOT THRIFT_LIBRARIES)
    
    MESSAGE("  --> ${THRIFT_LIBRARIES}")

ENDIF(THRIFT_FOUND AND NOT EXISTS "/${THRIFT_LIBRARIES}")

IF(THRIFT_FIND_REQUIRED)
    IF(NOT THRIFT_FOUND)
        MESSAGE(FATAL_ERROR "Unable to find thrift includes/libraries")
    ENDIF(NOT THRIFT_FOUND)

    LINTEL_FIND_PROGRAM(THRIFT thrift)
ELSE(THRIFT_FIND_REQUIRED)
    LINTEL_WITH_PROGRAM(THRIFT thrift)
ENDIF(THRIFT_FIND_REQUIRED)
