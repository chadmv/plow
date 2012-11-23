#include <QStringList>
#include <QString>

#include "plow/plow.h"

#include "JobTree.h"

namespace Plow { namespace Gui {

JobTree::JobTree(QWidget *parent) : QWidget(parent)
{
    layout = new QVBoxLayout(this);

    QStringList header;
    header << "Job" << "Cores" << "Max" << "Waiting";

    treeWidget = new QTreeWidget;
    treeWidget->setHeaderLabels(header);
    treeWidget->setColumnCount(4);
    treeWidget->setColumnWidth(0, 300);
    
    layout->addWidget(treeWidget);

}

void JobTree::updateJobs()
{

    std::vector<JobT> jobs;
    JobFilterT filter;

    std::vector<JobState::type> states;
    states.push_back(JobState::RUNNING);
    filter.states = states;

    getJobs(jobs, filter);

    for (std::vector<JobT>::iterator i = jobs.begin();
                                     i != jobs.end();
                                     ++i)
    {
        QStringList data;
        data << QString::fromStdString(i->name)
             << QString::number(i->runCores)
             << QString::number(i->maxCores)
             << QString::number(i->totals.waitingTaskCount);

        QTreeWidgetItem *item = new QTreeWidgetItem((QTreeWidget*)0, data);
        treeWidget->insertTopLevelItem(0, item);
    }
}


} // Gui
} // Plow