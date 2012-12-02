
#include "dock_widgets.h"

namespace Plow {
namespace Gui {

JobBoardDockWidget::JobBoardDockWidget(JobBoardWidget* widget, QWidget *parent) :
    QDockWidget("Job Board", parent)
{

    setWidget(widget);
}


TaskBoardDockWidget::TaskBoardDockWidget(TaskBoardWidget* widget, QWidget *parent) :
    QDockWidget("Tasks", parent)
{

    setWidget(widget);
}


NodeTableDockWidget::NodeTableDockWidget(NodeTableWidget* widget, QWidget *parent) :
    QDockWidget("Nodes", parent)
{

    setWidget(widget);
}


LogViewerDockWidget::LogViewerDockWidget(LogViewer* widget, QWidget *parent) :
    QDockWidget("Log Viewer", parent)
{
    setWidget(widget);
}

} // Plow
} // Gui

