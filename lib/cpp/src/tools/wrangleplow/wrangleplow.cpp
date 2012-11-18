#include <QTreeWidgetItem>
#include <vector>

#include "plow/plow.h"
#include "wrangleplow.h"

using namespace Plow;

namespace WranglePlow {

MainWindow::MainWindow()
{
    jobTree = new Gui::JobTree;
    jobTree->updateJobs();
    setCentralWidget(jobTree);
}

void MainWindow::closeEvent(QCloseEvent *event)
{

}

}