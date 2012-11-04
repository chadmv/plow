#include "wrangleplow.h"
#include "plow.h"

#include <QTreeWidgetItem>
#include <vector>

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