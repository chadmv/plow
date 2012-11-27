#include <QTreeWidgetItem>
#include <vector>

#include "plow/plow.h"
#include "gui/job_board.h"
#include "gui/dock_widgets.h"

#include "wrangleplow.h"

using namespace Plow;

namespace WranglePlow {

MainWindow::MainWindow()
{
    Plow::Gui::JobBoardWidget* jobBoard = new Plow::Gui::JobBoardWidget(this);
    Plow::Gui::JobBoardDockWidget *jobBoardDock = new Plow::Gui::JobBoardDockWidget(jobBoard, this);
    
    Plow::Gui::TaskBoardWidget* taskBoard = new Plow::Gui::TaskBoardWidget(this);
    Plow::Gui::TaskBoardDockWidget *taskBoardDock = new Plow::Gui::TaskBoardDockWidget(taskBoard, this);

    addDockWidget(Qt::TopDockWidgetArea, jobBoardDock);
    addDockWidget(Qt::BottomDockWidgetArea, taskBoardDock);
}

void MainWindow::closeEvent(QCloseEvent *event)
{

}

} // WranglePlow