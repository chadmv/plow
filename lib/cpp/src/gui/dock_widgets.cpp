
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
}


TaskPieDockWidget::TaskPieDockWidget(TaskPieWidget* widget, QWidget *parent) :
    QDockWidget("Task Pie", parent)
{
    setWidget(widget);
}

void LogViewerDockWidget::addTask(const QString &taskId) {
    qobject_cast<TabbedLogCollection*>(widget())->addTask(taskId);
    show();
}

void LogViewerDockWidget::closeEvent(QCloseEvent *event) {
    qobject_cast<TabbedLogCollection*>(widget())->closeAllTabs();
    QDockWidget::closeEvent(event);
}


} // Plow
} // Gui

