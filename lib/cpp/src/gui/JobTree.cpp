#include "JobTree.h"
#include "plow.h"

#include <QStringList>
#include <QString>

namespace Plow { namespace Gui {

JobTree::JobTree(QWidget *parent) : QWidget(parent)
{
    layout = new QVBoxLayout(this);

    QStringList header;
    header << "Job" << "Cores" << "Max" << "Waiting";

    treeWidget = new QTreeWidget;
    treeWidget->setHeaderLabels(header);
    treeWidget->setColumnCount(4);

    layout->addWidget(treeWidget);
}

void JobTree::updateJobs()
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
        QStringList data;
        data << QString::fromStdString(i->name)
             << QString::number(i->runningCoreCount)
             << QString::number(i->maxCores)
             << QString::number(i->waitingTaskCount);

        QTreeWidgetItem *item = new QTreeWidgetItem((QTreeWidget*)0, data);
        treeWidget->insertTopLevelItem(0, item);
    }
}

}
}