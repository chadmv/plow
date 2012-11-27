PROJECT(PLOW_GUI)

INCLUDE(${QT_USE_FILE})

SET(PLOW_GUI_SRCS_CXX
    JobTree.cpp
    nodemodel.cpp 
    nodetablewidget.cpp
    job_board.cpp
    task_board.cpp
    dock_widgets.cpp
    event.cpp
)

SET(PLOW_GUI_MOC_HDRS
    JobTree.h
    nodemodel.h 
    nodetablewidget.h
    job_board.h
    task_board.h
    dock_widgets.h
    event.h
)

SET(PLOW_GUI_UI_FILES

)

QT4_WRAP_UI( PLOW_GUI_SRCS_CXX ${PLOW_GUI_UI_FILES} )
QT4_WRAP_CPP( PLOW_GUI_SRCS_CXX ${PLOW_GUI_MOC_HDRS} )

SOURCE_GROUP("Generated Files" REGULAR_EXPRESSION "(ui_.*h)$")
SOURCE_GROUP("UI Files" REGULAR_EXPRESSION ".*.ui$")

add_library(plowgui SHARED ${PLOW_GUI_SRCS_CXX})
target_link_libraries(plowgui plow ${QT_LIBRARIES})

add_subdirectory(examples)
