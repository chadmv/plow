#include "wrangleplow.h"
#include "plow.h"

#include <QTreeWidgetItem>
#include <vector>

using namespace Plow;

namespace WranglePlow {

MainWindow::MainWindow()
{
    QStringList header;
    header << "Job" << "Cores" << "Max" << "Waiting";

    treeWidget = new QTreeWidget;
    treeWidget->setHeaderLabels(header);
    treeWidget->setColumnCount(4);

    setCentralWidget(treeWidget);
}

void MainWindow::closeEvent(QCloseEvent *event)
{

}

void MainWindow::updateJobs()
{
    PlowClient* client = getClient();

    std::vector<JobT> jobs;
    JobFilterT filter;

    std::vector<JobState::type> states;
    states.push_back(JobState::RUNNING);
    filter.__set_states(states);

    client->getJobs(jobs, filter);

    for (std::vector<JobT>::iterator i = jobs.begin();
                                     i != jobs.end();
                                     ++i)
    {
        QTreeWidgetItem *item = new QTreeWidgetItem((QTreeWidget*)0,
            QStringList(QString(i->name.c_str())));
        treeWidget->insertTopLevelItem(0, item);
    }
}

}