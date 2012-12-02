#include <QTreeWidgetItem>
#include <vector>

#include "plow/plow.h"
#include "gui/job_board.h"
#include "gui/nodemodel.h"
#include "gui/logviewer.h"
#include "gui/dock_widgets.h"

#include "wrangleplow.h"

using namespace Plow;

namespace WranglePlow {

MainWindow::MainWindow()
{
    setDockNestingEnabled(true);

    Gui::JobBoardWidget* jobBoard = new Gui::JobBoardWidget(this);
    Gui::JobBoardDockWidget *jobBoardDock = new Gui::JobBoardDockWidget(jobBoard, this);
    
    Gui::TaskBoardWidget* taskBoard = new Gui::TaskBoardWidget(this);
    Gui::TaskBoardDockWidget *taskBoardDock = new Gui::TaskBoardDockWidget(taskBoard, this);

    Gui::NodeTableWidget *nodeWidget = new Gui::NodeTableWidget(this);
    Gui::NodeTableDockWidget *nodeTableDock = new Gui::NodeTableDockWidget(nodeWidget, this);

    addDockWidget(Qt::TopDockWidgetArea, jobBoardDock);
    addDockWidget(Qt::BottomDockWidgetArea, taskBoardDock);
    splitDockWidget(taskBoardDock, nodeTableDock, Qt::Vertical);

    Gui::LogViewer *logView = new Gui::LogViewer(this);
    Gui::LogViewerDockWidget *logViewerDock = new Gui::LogViewerDockWidget(logView, this);
    logViewerDock->setFloating(true);
    logViewerDock->resize(600,600);
    logViewerDock->move(this->geometry().topRight()-QPoint(250,100));

    nodeWidget->load();
}

void MainWindow::closeEvent(QCloseEvent *event)
{

}

} // WranglePlow
