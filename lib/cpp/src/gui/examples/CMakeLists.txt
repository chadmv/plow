PROJECT(PLOW_GUI_EXAMPLES)

#
# NodeWidget
#
set (nodeWidget_MOC_HEADERS fixture_p.h)
set (nodeWidget_SOURCE nodeWidget.cpp)

QT4_WRAP_CPP (nodeWidget_SOURCE ${nodeWidget_MOC_HEADERS} )

IF(APPLE)
	ADD_EXECUTABLE(nodeWidget MACOSX_BUNDLE 
		${nodeWidget_SOURCE})	
ELSEIF(UNIX)
	ADD_EXECUTABLE(nodeWidget 
		${nodeWidget_SOURCE})
ENDIF()

add_dependencies(nodeWidget plowgui)
TARGET_LINK_LIBRARIES(nodeWidget plowgui ${QT_LIBRARIES})