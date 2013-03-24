#
# (c) Copyright 2008, Hewlett-Packard Development Company, LP
#
#  See the file named COPYING for license details

# Macro for finding binaries, headers, and libraries; also does all of
# the WITH_xxx support so users can disable libraries that they have
# installed but don't want to use.  Normally you would use the
# LINTEL_WITH_* or LINTEL_REQUIRED_* variants of the macros.
#
# All of the macros will set LINTEL_FIND_ALL_NOTFOUND to the list of
# variables that were not found.

# LINTEL_FIND_HEADER(variable header) sets ${variable}_INCLUDE_DIR,
#   ${variable}_INCLUDES, and ${variable}_ENABLED.
#
#   Normally you would use LINTEL_WITH_HEADER or LINTEL_REQUIRED_HEADER
#
#   The header must exist if ${variable}_FIND_REQUIRED is set.
#   It also sets up an option WITH_${variable} unless
#   ${variable}_FIND_REQUIRED is on.  It sets ${variable}_ENABLED based
#   on finding the header and WITH_${variable}; you can set 
#   ${variable}_EXTRA_INCLUDE_PATHS to specify additional locations it 
#   should look, and ${variable}_EXTRA_INCLUDES to specify extra directories 
#   to put in the ${variable}_INCLUDES variable.

# LINTEL_FIND_LIBRARY(variable header libname) sets
#   ${variable}_LIBRARIES, and ${variable}_ENABLED after calling
#   LINTEL_FIND_HEADER to find the header.
#
#   Normally you would use LINTEL_WITH_LIBRARY or LINTEL_REQUIRED_LIBRARY
#
#   The library must exist if ${variable}_FIND_REQUIRED is set.  You
#   can set ${variable}_EXTRA_LIBRARIES to specify extra libraries to
#   put in the ${variable}_LIBRARIES variable.  ${variable}_LIBRARY
#   will be set to the file containing the library without any of its
#   dependencies.

# LINTEL_FIND_PROGRAM(variable program) sets ${variable}_PATH The
#   program must exist if ${variable}_FIND_REQUIRED is set.  The search
#   will look in ${CMAKE_CURRENT_BINARY_DIR}, ${variable}_EXTRA_PATHS, 
#   ${CMAKE_INSTALL_PREFIX}/bin, and the default system paths.
#
#   Normally you would use LINTEL_WITH_PROGRAM or LINTEL_REQUIRED_PROGRAM

# LINTEL_WITH_*(...) adds an option WITH_${variable}, and sets
#   ${variable}_ENABLED if the thing was found and we should use it.  If
#   we should use it and it was not found, a message is printed out
#   explaining what was not found.  If ${variable}_MISSING_EXTRA is set,
#   that string is also printed.

# LINTEL_REQUIRED_*(...) tries to find the thing and produces and
#   error message if it is not found.  If ${variable}_MISSING_EXTRA is
#   set, that string is also printed.

# LINTEL_FIND_LIBRARY_CMAKE_INCLUDE_FILE(variable header libname) is
# the macro for putting in one of the FindX files; it properly chooses
# between LINTEL_FIND_LIBRARY and LINTEL_WITH_LIBRARY depending on
# whether ${variable}_FIND_REQUIRED is set. Boilerplate for the header
# of the FindX file can be found below next to the macro.

# LINTEL_BOOST_EXTRA(variable header libname) assumes you have already
# included FindBoost; it searches for header and libname and checks
# that the directories are compatible.  If ${variable}_FIND_REQUIRED
# is set, it uses LINTEL_FIND_LIBRARY, otherwise LINTEL_WITH_LIBRARY.
# You can specify the special value None for libname if the boost library
# is header only.

# LINTEL_FIND_PERL_MODULE(module_name variable) searches for a
# particular perl module, and sets ${variable}_ENABLED 

# You can set the LINTEL_FIND_DEBUG variable to enable debugging

# TODO: there is a bunch of duplicate code in the below macros,
# eliminate it.

# SET(LINTEL_FIND_DEBUG ON)
MACRO(LINTEL_FIND_DEBUG message)
    IF(LINTEL_FIND_DEBUG)
        MESSAGE("lintel-find-debug: ${message}")
    ENDIF(LINTEL_FIND_DEBUG)
ENDMACRO(LINTEL_FIND_DEBUG)

### headers

# TODO: for this macro and the library one, recheck that the file
# still exists, and redo the search if it vanished.

MACRO(LINTEL_FIND_HEADER variable header)
    LINTEL_FIND_DEBUG("searching for ${header} --> ${variable}")
    IF(${variable}_INCLUDE_DIR)
        LINTEL_FIND_DEBUG("  in cache: ${${variable}_INCLUDE_DIR}")
        # Already in cache, be silent
        SET(${variable}_FIND_QUIETLY ON)
    ENDIF(${variable}_INCLUDE_DIR)
    
    # This is the recommended cmake idiom to use a locally built version
    # of a header in preference to the system one.

    FIND_PATH(${variable}_INCLUDE_DIR ${header}
        PATHS ${CMAKE_INSTALL_PREFIX}/include
        NO_DEFAULT_PATH
    )
    LINTEL_FIND_DEBUG("  install_prefix --> ${${variable}_INCLUDE_DIR}")

    IF(${variable}_EXTRA_INCLUDE_PATHS)
	FIND_PATH(${variable}_INCLUDE_DIR ${header}
	          PATHS ${${variable}_EXTRA_INCLUDE_PATHS} NO_DEFAULT_PATH)
        LINTEL_FIND_DEBUG("  extra-includes --> ${${variable}_INCLUDE_DIR}")
    ENDIF(${variable}_EXTRA_INCLUDE_PATHS)

    FIND_PATH(${variable}_INCLUDE_DIR ${header})
    LINTEL_FIND_DEBUG("  generic-search --> ${${variable}_INCLUDE_DIR}")

    MARK_AS_ADVANCED(${variable}_INCLUDE_DIR)

    IF(${variable}_FIND_REQUIRED)
        IF(NOT ${variable}_INCLUDE_DIR)
            MESSAGE(STATUS "Looked for header file ${header} in ${CMAKE_INSTALL_PREFIX}/include, system paths, and '${${variable}_EXTRA_INCLUDE_PATHS}' (if set)")
	    IF(DEFINED ${variable}_MISSING_EXTRA)
	        MESSAGE(STATUS "${${variable}_MISSING_EXTRA}")
	    ENDIF(DEFINED ${variable}_MISSING_EXTRA)
            MESSAGE(FATAL_ERROR "ERROR: Could NOT find header file ${header}")
        ENDIF(NOT ${variable}_INCLUDE_DIR)
	SET(${variable}_ENABLED ON)
    ENDIF(${variable}_FIND_REQUIRED)

    IF(${variable}_INCLUDE_DIR)
        IF(NOT ${variable}_FIND_QUIETLY)
            MESSAGE(STATUS "Found header ${header} in ${${variable}_INCLUDE_DIR}")
        ENDIF(NOT ${variable}_FIND_QUIETLY)
    ELSE(${variable}_INCLUDE_DIR)
        SET(${variable}_INCLUDE_DIR "")
	LIST(APPEND LINTEL_FIND_ALL_NOTFOUND ${variable})
    ENDIF(${variable}_INCLUDE_DIR)

    SET(${variable}_INCLUDES ${${variable}_INCLUDE_DIR}
	${${variable}_EXTRA_INCLUDES})
ENDMACRO(LINTEL_FIND_HEADER)

MACRO(LINTEL_WITH_HEADER variable header)
    LINTEL_FIND_HEADER(${variable} ${header})

    SET(WITH_${variable} ON CACHE BOOL "Enable compilation depending on header file ${header}")
    IF(WITH_${variable} AND ${variable}_INCLUDE_DIR)
	SET(${variable}_ENABLED ON)
    ELSE(WITH_${variable} AND ${variable}_INCLUDE_DIR)
        SET(${variable}_ENABLED OFF)
    ENDIF(WITH_${variable} AND ${variable}_INCLUDE_DIR)

    IF(WITH_${variable} AND NOT ${variable}_ENABLED)
        MESSAGE("WITH_${variable} on, but could not find header file ${header}")
        IF(DEFINED ${variable}_MISSING_EXTRA)
	    MESSAGE("${${variable}_MISSING_EXTRA}")
        ENDIF(DEFINED ${variable}_MISSING_EXTRA)
    ENDIF(WITH_${variable} AND NOT ${variable}_ENABLED)
ENDMACRO(LINTEL_WITH_HEADER)  

MACRO(LINTEL_REQUIRED_HEADER variable header)
    SET(${variable}_FIND_REQUIRED ON)
    LINTEL_FIND_HEADER(${variable} ${header})
ENDMACRO(LINTEL_REQUIRED_HEADER variable header)

### Libraries

# TODO: consider a check that the library and header share most of their
# prefix and generate a warning if they don't.

MACRO(LINTEL_FIND_LIBRARY_ONE variable libname)
    LINTEL_FIND_DEBUG("  pre-find: ${${variable}_LIBRARY}")
    FIND_LIBRARY(${variable}_LIBRARY 
        NAMES ${libname} 
        PATHS ${CMAKE_INSTALL_PREFIX}/lib
        NO_DEFAULT_PATH)
    LINTEL_FIND_DEBUG("  install-prefix: ${${variable}_LIBRARY}")

    IF(${variable}_EXTRA_LIBRARY_PATHS)
        FIND_LIBRARY(${variable}_LIBRARY
            NAMES ${libname}
            PATHS ${${variable}_EXTRA_LIBRARY_PATHS} 
            NO_DEFAULT_PATH)
    ENDIF(${variable}_EXTRA_LIBRARY_PATHS)
    LINTEL_FIND_DEBUG("  extra-paths: ${${variable}_LIBRARY}")

    FIND_LIBRARY(${variable}_LIBRARY NAMES ${libname})
    LINTEL_FIND_DEBUG("  generic-search: ${${variable}_LIBRARY}")

    MARK_AS_ADVANCED(${variable}_LIBRARY)
ENDMACRO(LINTEL_FIND_LIBRARY_ONE variable libname)

MACRO(LINTEL_FIND_LIBRARY variable header liblist_str)
    SET(liblist ${liblist_str}) # necessary to allow SEPARATE_ARGUMENTS to work
    LINTEL_FIND_HEADER(${variable} ${header})

    LINTEL_FIND_DEBUG("searching for library ${liblist} --> ${variable}")

    SEPARATE_ARGUMENTS(liblist)

    LINTEL_FIND_DEBUG("  post-separate ${liblist}")

    FOREACH(libname ${liblist})
        LINTEL_FIND_LIBRARY_ONE(${variable} ${libname})
    ENDFOREACH(libname)

    IF (${variable}_INCLUDE_DIR AND ${variable}_LIBRARY)
        SET(${variable}_FOUND TRUE)
        SET(${variable}_LIBRARIES ${${variable}_LIBRARY} 
	    ${${variable}_EXTRA_LIBRARIES})
    ELSE (${variable}_INCLUDE_DIR AND ${variable}_LIBRARY)
        SET(${variable}_FOUND FALSE)
        SET(${variable}_LIBRARIES ${variable}-NOTFOUND)
	LIST(APPEND LINTEL_FIND_ALL_NOTFOUND ${variable})
    ENDIF (${variable}_INCLUDE_DIR AND ${variable}_LIBRARY)

    IF (${variable}_FIND_REQUIRED)
        IF(NOT ${variable}_FOUND)
            MESSAGE(STATUS "Looked for library named ${libname} in ${CMAKE_INSTALL_PREFIX}/lib and system paths")
            MESSAGE(STATUS "got: ${variable}_INCLUDE_DIR=${${variable}_INCLUDE_DIR}")
            MESSAGE(STATUS "got: ${variable}_LIBRARY=${${variable}_LIBRARY}")
            MESSAGE(FATAL_ERROR "ERROR: Could NOT find ${libname} library")
        ENDIF(NOT ${variable}_FOUND)
    ENDIF (${variable}_FIND_REQUIRED)

    IF(NOT ${variable}_FIND_QUIETLY)
	IF(${variable}_FOUND)
            MESSAGE(STATUS "Found library ${libname} as ${${variable}_LIBRARY}")
	ENDIF(${variable}_FOUND)
    ENDIF(NOT ${variable}_FIND_QUIETLY)
ENDMACRO(LINTEL_FIND_LIBRARY)

MACRO(LINTEL_WITH_LIBRARY variable header libname)
    LINTEL_FIND_LIBRARY(${variable} ${header} ${libname})

    SET(WITH_${variable} ON CACHE BOOL "Enable compilation depending on library ${libname}")
    IF(WITH_${variable} AND ${variable}_FOUND)
	SET(${variable}_ENABLED ON)
    ELSE(WITH_${variable} AND ${variable}_FOUND)
        SET(${variable}_ENABLED OFF)
    ENDIF(WITH_${variable} AND ${variable}_FOUND)

    IF(WITH_${variable} AND NOT ${variable}_ENABLED)
        MESSAGE("WITH_${variable} on, but could not find header file ${header} or library ${libname}")
        IF(DEFINED ${variable}_MISSING_EXTRA)
	    MESSAGE("${${variable}_MISSING_EXTRA}")
        ENDIF(DEFINED ${variable}_MISSING_EXTRA)
    ENDIF(WITH_${variable} AND NOT ${variable}_ENABLED)
ENDMACRO(LINTEL_WITH_LIBRARY)  

MACRO(LINTEL_REQUIRED_LIBRARY variable header libname)
    SET(${variable}_FIND_REQUIRED ON)
    LINTEL_FIND_LIBRARY(${variable} ${header} ${libname})
ENDMACRO(LINTEL_REQUIRED_LIBRARY variable header libname)

### Programs

MACRO(LINTEL_FIND_PROGRAM variable program)
    IF(${variable}_PATH)
	IF(NOT EXISTS "${${variable}_PATH}")
	    MESSAGE("WARNING: ${${variable}_PATH} vanished.")
	    SET(${variable}_PATH ${variable}-NOTFOUND)
	ELSE(NOT EXISTS "${${variable}_PATH}")
	    # Already in cache, be silent
	    SET(${variable}_FIND_QUIETLY ON)
	ENDIF(NOT EXISTS "${${variable}_PATH}")
    ENDIF(${variable}_PATH)

    FIND_PROGRAM(${variable}_PATH ${program}
  		 PATHS ${CMAKE_CURRENT_BINARY_DIR} 
	               ${${variable}_EXTRA_PATHS} 
	               ${CMAKE_INSTALL_PREFIX}/bin
		 NO_DEFAULT_PATH)

    FIND_PROGRAM(${variable}_PATH ${program})

    MARK_AS_ADVANCED(${variable}_PATH)

    IF(${variable}_FIND_REQUIRED)
        IF(NOT ${variable}_PATH)
            MESSAGE("Looked for program ${program} in ${CMAKE_CURRENT_BINARY_DIR} ${${variable}_EXTRA_PATHS} ${CMAKE_INSTALL_PREFIX}/bin and system paths")
	    IF(DEFINED ${variable}_MISSING_EXTRA)
	        MESSAGE(STATUS "${${variable}_MISSING_EXTRA")
	    ENDIF(DEFINED ${variable}_MISSING_EXTRA)
	    MESSAGE(FATAL_ERROR "ERROR: Could NOT find program ${program}")
        ENDIF(NOT ${variable}_PATH)
    ENDIF(${variable}_FIND_REQUIRED)

    IF(${variable}_PATH)
        IF(NOT ${variable}_FIND_QUIETLY)
            MESSAGE(STATUS "Found program ${program} as ${${variable}_PATH}")
        ENDIF(NOT ${variable}_FIND_QUIETLY)
    ELSE(${variable}_PATH)
        LIST(APPEND LINTEL_FIND_ALL_NOTFOUND ${variable})
    ENDIF(${variable}_PATH)
ENDMACRO(LINTEL_FIND_PROGRAM)

MACRO(LINTEL_WITH_PROGRAM variable program)
    LINTEL_FIND_PROGRAM(${variable} ${program})

    SET(WITH_${variable} ON CACHE BOOL "Enable compilation using program ${program}")
    IF(WITH_${variable} AND ${variable}_PATH)
	SET(${variable}_ENABLED ON)
    ELSE(WITH_${variable} AND ${variable}_PATH)
        SET(${variable}_ENABLED OFF)
    ENDIF(WITH_${variable} AND ${variable}_PATH)

    IF(WITH_${variable} AND NOT ${variable}_ENABLED)
        MESSAGE("WITH_${variable} on, but could NOT find program ${program}")
        IF(DEFINED ${variable}_MISSING_EXTRA)
	    MESSAGE("${${variable}_MISSING_EXTRA}")
        ENDIF(DEFINED ${variable}_MISSING_EXTRA)
    ENDIF(WITH_${variable} AND NOT ${variable}_ENABLED)
ENDMACRO(LINTEL_WITH_PROGRAM)  

MACRO(LINTEL_REQUIRED_PROGRAM variable program)
    SET(${variable}_FIND_REQUIRED ON)
    LINTEL_FIND_PROGRAM(${variable} ${program})
ENDMACRO(LINTEL_REQUIRED_PROGRAM variable program)

# Find the Variable includes and library
#
#  set VARIABLE_FIND_REQUIRED to require the VARIABLE library
#  otherwise, the user will be given a WITH_VARIABLE option.
# 
#  VARIABLE_INCLUDE_DIR - where to find header file for Variable
#  VARIABLE_INCLUDES    - all includes needed for Variable
#  VARIABLE_LIBRARY     - where to find the library for Variable
#  VARIABLE_LIBRARIES   - all libraries needed for Variable
#  VARIABLE_ENABLED     - true if VARIABLE is enabled

# If you are creating FindVariable.cmake, then you want to put the
# documentation from above in, substitute for variable as appropriate,
# and set the variables VARIABLE_EXTRA_INCLUDES and
# VARIABLE_EXTRA_LIBRARIES before calling the
# LINTEL_FIND_LIBRARY_CMAKE_INCLUDE_FILE macro to specify the extra
# includes/libraries needed.

MACRO(LINTEL_FIND_LIBRARY_CMAKE_INCLUDE_FILE variable header libname)
    IF(${variable}_FIND_REQUIRED)
	LINTEL_FIND_LIBRARY(${variable} ${header} ${libname})
    ELSE(${variable}_FIND_REQUIRED)
	LINTEL_WITH_LIBRARY(${variable} ${header} ${libname})
    ENDIF(${variable}_FIND_REQUIRED)
ENDMACRO(LINTEL_FIND_LIBRARY_CMAKE_INCLUDE_FILE)
 
### LINTEL_BOOST_EXTRA

MACRO(LINTEL_BOOST_EXTRA variable header libname)
    LINTEL_FIND_DEBUG("Boost_INCLUDE_DIRS=${Boost_INCLUDE_DIRS}")
    LINTEL_FIND_DEBUG("Boost_LIBRARY_DIRS=${Boost_LIBRARY_DIRS}")
    IF("${Boost_INCLUDE_DIRS}" STREQUAL "")
        MESSAGE("WARNING: Did not include FindBoost, automatically including")
	INCLUDE(FindBoost)
    ENDIF("${Boost_INCLUDE_DIRS}" STREQUAL "")

    SET(${variable}_EXTRA_INCLUDE_PATHS ${Boost_INCLUDE_DIRS})

    IF(${libname} STREQUAL "None")
        IF(${variable}_FIND_REQUIRED)
            LINTEL_FIND_HEADER(${variable} ${header})
        ELSE(${variable}_FIND_REQUIRED)
    	    LINTEL_WITH_HEADER(${variable} ${header})
        ENDIF(${variable}_FIND_REQUIRED)
    ELSE(${libname} STREQUAL "None")
        SET(${variable}_EXTRA_LIBRARY_PATHS ${Boost_LIBRARY_DIRS})
        IF(${variable}_FIND_REQUIRED)
            LINTEL_FIND_LIBRARY(${variable} ${header} ${libname})
        ELSE(${variable}_FIND_REQUIRED)
    	    LINTEL_WITH_LIBRARY(${variable} ${header} ${libname})
        ENDIF(${variable}_FIND_REQUIRED)
    ENDIF(${libname} STREQUAL "None")

    IF(${variable}_ENABLED)
        IF(NOT "${${variable}_INCLUDES}" STREQUAL "${Boost_INCLUDE_DIRS}") 
            MESSAGE("You can force the boost include/library directories by specifying the cmake options")
            MESSAGE("  -DBoost_INCLUDE_DIR=path -DBoost_LIBRARY_DIRS=path")
            MESSAGE("  If you are using deptool, you can set these in DEPTOOL_CMAKE_FLAGS")
            MESSAGE(FATAL_ERROR "Error: different ${header} / boost include dirs '${${variable}_INCLUDES}' != '${Boost_INCLUDE_DIRS}' " )
	ENDIF(NOT "${${variable}_INCLUDES}" STREQUAL "${Boost_INCLUDE_DIRS}") 

	IF(NOT ${libname} STREQUAL "None")
	    GET_FILENAME_COMPONENT(LBE_TMP ${${variable}_LIBRARY} PATH)
	    IF (0)
	    ELSEIF("${Boost_LIBRARY_DIRS}" STREQUAL "/usr/lib"
	           AND EXISTS "/usr/lib64/libboost_program_options.so"
		   AND "${LBE_TMP}" STREQUAL "/usr/lib64")
		# First seen on OpenSuSE 11 x86_64, RHEL5 has libraries
   	        # in both /usr/lib and /usr/lib64, but cmake search
                # prefers the lib64 versions
	        MESSAGE("Warning: cmake determined the Boost library dir incorrectly. It should be /usr/lib64 not /usr/lib")
		SET(Boost_LIBRARY_DIRS /usr/lib64)
	    ENDIF(0)
	        
	    IF(NOT "${LBE_TMP}" STREQUAL "${Boost_LIBRARY_DIRS}")
                MESSAGE("You can force the boost include/library directories by specifying the cmake options")
                MESSAGE("  -DBoost_INCLUDE_DIR=path -DBoost_LIBRARY_DIRS=path")
                MESSAGE("  If you are using deptool, you can set these in DEPTOOL_CMAKE_FLAGS")
 	        MESSAGE(FATAL_ERROR "Error: different ${libname} / boost lib dirs ${${variable}_LIBRARY} -> '${LBE_TMP}' != '${Boost_LIBRARY_DIRS}' ")	
  	    ENDIF(NOT "${LBE_TMP}" STREQUAL "${Boost_LIBRARY_DIRS}")
	ENDIF(NOT ${libname} STREQUAL "None")
    ENDIF(${variable}_ENABLED)
ENDMACRO(LINTEL_BOOST_EXTRA variable header libname)

# This macro searches for the perl module ${module_name}. It sets
# ${variable}_FOUND to whether the module was found.  It creates a
# configuration variable WITH_${variable} so the user can control
# whether this perl module should be used.  Finally in combines these
# into ${variable}_ENABLED for use in the CMakeLists.txt to determine
# if we should build using this module.

MACRO(LINTEL_FIND_PERL_MODULE variable module_name)
    IF("${PERL_FOUND}" STREQUAL "")
        INCLUDE(FindPerl)
    ENDIF("${PERL_FOUND}" STREQUAL "")

    IF(${variable}_FOUND)
        # ... nothing to do, already found it
    ELSE(${variable}_FOUND)
         SET(LFPM_found OFF)
         IF(PERL_FOUND)
	     # Tried OUTPUT_QUIET and ERROR_QUIET but with cmake 2.4-patch 5 
	     # this didn't seem to make it quiet.
             EXEC_PROGRAM(${PERL_EXECUTABLE}
                          ARGS -e "\"use lib '${CMAKE_INSTALL_PREFIX}/share/perl5'; use ${module_name};\""
                          RETURN_VALUE LFPM_return_value
		          OUTPUT_VARIABLE LFPM_output
			  ERROR_VARIABLE LFPM_error_output)
             IF("${LFPM_return_value}" STREQUAL 0)
                 SET(LFPM_found ON)
             ENDIF("${LFPM_return_value}" STREQUAL 0)
         ENDIF(PERL_FOUND)
         SET(${variable}_FOUND ${LFPM_found} CACHE BOOL "Found ${module_name} perl module" FORCE)
         MARK_AS_ADVANCED(${variable}_FOUND)
	 IF(${variable}_FOUND)
	     MESSAGE(STATUS "Found perl module ${module_name}")
	 ELSE(${variable}_FOUND)
	     MESSAGE("Could NOT find perl module ${module_name} in default perl paths")
	     MESSAGE("  or ${CMAKE_INSTALL_PREFIX}/share/perl5")
	     IF(${variable}_FIND_REQUIRED)
                 MESSAGE(FATAL_ERROR "ERROR: Could NOT find required perl module ${module_name}")
	     ENDIF(${variable}_FIND_REQUIRED)
             LIST(APPEND LINTEL_FIND_ALL_NOTFOUND ${variable})
	 ENDIF(${variable}_FOUND)
    ENDIF(${variable}_FOUND)
ENDMACRO(LINTEL_FIND_PERL_MODULE)

MACRO(LINTEL_WITH_PERL_MODULE variable module_name)
    SET(WITH_${variable} "ON" CACHE BOOL "Enable use of the ${module_name} perl module")
    IF(WITH_${variable})
        LINTEL_FIND_PERL_MODULE(${variable} ${module_name})
        IF(${variable}_FOUND)
            SET(${variable}_ENABLED ON)
        ELSE(${variable}_FOUND)
            SET(${variable}_ENABLED OFF)
        ENDIF(${variable}_FOUND)
    ELSE(WITH_${variable})
        SET(${variable}_ENABLED OFF)
    ENDIF(WITH_${variable})
ENDMACRO(LINTEL_WITH_PERL_MODULE)

MACRO(LINTEL_REQUIRED_PERL_MODULE variable module_name)
    SET(${variable}_FIND_REQUIRED ON)
    LINTEL_FIND_PERL_MODULE(${variable} ${module_name})
ENDMACRO(LINTEL_REQUIRED_PERL_MODULE)


  