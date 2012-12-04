
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


LogViewerDockWidget::LogViewerDockWidget(TabbedLogCollection* widget, QWidget *parent) :
    QDockWidget("Log Viewer", parent)
{
    setWidget(widget);

    connect(this, SIGNAL(visibilityChanged(bool)), this, SLOT(handleVisibilityChange(bool)));
}


void LogViewerDockWidget::addTask(const QString &taskId) {
    qobject_cast<TabbedLogCollection*>(widget())->addTask(taskId);
    show();
}

void LogViewerDockWidget::handleVisibilityChange(bool vis) {
    // TODO: Properly figure out when the dock widget is hidden but NOT
    // nested as a tab on another dock. If so, then clear all the log tabs.
//    if (!vis && isFloating())
//        qobject_cast<TabbedLogCollection*>(widget())->closeAllTabs();
}

} // Plow
} // Gui

