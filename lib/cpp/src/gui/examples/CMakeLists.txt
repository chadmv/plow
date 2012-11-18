PROJECT(PLOW_GUI_EXAMPLES)

#
# NodeWidget
#
set (nodeWidget_MOC_HEADERS fixture_p.h)
set (nodeWidget_SOURCE nodewidget.cpp)

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
