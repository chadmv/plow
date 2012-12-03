PROJECT(PLOW_GUI_EXAMPLES)

#
# NodeWidget
#
set (nodeWidget_MOC_HEADERS
    fixture_p.h
)

set (nodeWidget_SOURCE
    fixture_p.cpp
    nodewidget.cpp
)

QT4_WRAP_CPP (nodeWidget_SOURCE ${nodeWidget_MOC_HEADERS} )

IF(APPLE)
	ADD_EXECUTABLE(nodewidget MACOSX_BUNDLE 
		${nodeWidget_SOURCE})	
ELSEIF(UNIX)
	ADD_EXECUTABLE(nodewidget 
		${nodeWidget_SOURCE})
ENDIF()

add_dependencies(nodewidget plowgui)
TARGET_LINK_LIBRARIES(nodewidget plowgui ${QT_LIBRARIES})

#
# LogViewer
#
set (logViewer_SOURCE logViewer.cpp)

IF(APPLE)
        ADD_EXECUTABLE(logviewer MACOSX_BUNDLE
                ${logViewer_SOURCE})
ELSEIF(UNIX)
        ADD_EXECUTABLE(logviewer
                ${logViewer_SOURCE})
ENDIF()

add_dependencies(logviewer plowgui)
TARGET_LINK_LIBRARIES(logviewer plowgui ${QT_LIBRARIES})

#
# Task Pie
#
set (taskPie_SOURCE task_pie.cpp)

IF(APPLE)
        ADD_EXECUTABLE(taskpie MACOSX_BUNDLE
                ${taskPie_SOURCE})
ELSEIF(UNIX)
        ADD_EXECUTABLE(taskpie
                ${taskPie_SOURCE})
ENDIF()

add_dependencies(taskpie plowgui)
TARGET_LINK_LIBRARIES(taskpie plowgui ${QT_LIBRARIES})