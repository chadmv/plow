#include <QStringList>
#include <QString>
#include <QTimer>
#include <QVBoxLayout>
#include <QDateTime>
#include <QApplication>
#include <QAction>

#include "plow/plow.h"

#include "event.h"
#include "job_board.h"
#include "simple_progressbar_widget.h"

namespace Plow { 
namespace Gui {

JobBoardWidget::JobBoardWidget(QWidget *parent) : 
    QWidget(parent),
    itemIndex(new ItemIndex()),
    treeWidget(new QTreeWidget(this)),
    updateCounter(1)
{
    QVBoxLayout* layout = new QVBoxLayout(this);

    QStringList header;
    header << "Job" << "Cores" << "Min" << "Waiting" << "MaxRSS" << "Progress";

    treeWidget->setHeaderLabels(header);
    treeWidget->setColumnCount(6);
    treeWidget->setColumnWidth(0, 400);
    treeWidget->setContextMenuPolicy(Qt::ActionsContextMenu);
    treeWidget->setSelectionMode(QAbstractItemView::ExtendedSelection);
    layout->addWidget(treeWidget);

    defineActions();
    load();
    

    QTimer* refreshTimer = new QTimer(this);
    connect(refreshTimer, SIGNAL(timeout()), this, SLOT(refresh()));
    refreshTimer->start(1000);

    connect(treeWidget, SIGNAL(itemClicked(QTreeWidgetItem*, int)),
        this, SLOT(itemSelected(QTreeWidgetItem*, int)));

    connect(this, SIGNAL(jobSelected(QString)),
        EventManager::getInstance(), SLOT(handleJobSelected(QString)));
}

JobBoardWidget::~JobBoardWidget()
{
    
    delete treeWidget;
    delete itemIndex;
}

void JobBoardWidget::defineActions()
{
 
    QAction* jobKillAction = new QAction(tr("Kill Job"), treeWidget);
    connect(jobKillAction, SIGNAL(triggered()), this, SLOT(onJobKill()));
    treeWidget->addAction(jobKillAction);   
}

void JobBoardWidget::onJobKill()
{
    QList<QTreeWidgetItem *> items = treeWidget->selectedItems();
    if (!items.isEmpty())
    {
        for (int i = 0; i < items.size(); ++i) 
        {
            QTreeWidgetItem* item = items[i];
                    
            JobT job;
            std::string job_name = item->text(0).toStdString();
            try
            {
                getActiveJob(job, job_name);
                Plow::killJob(job);
            }
            catch(std::exception& e) 
            {
                // TODO: not sure what to do with exeptions here
                qDebug("JobBoardWidget::onKillAction: Exception thrown: %s", e.what());
            }
        }

    }
}

void JobBoardWidget::itemSelected(QTreeWidgetItem *item, int column)
{

    if (item->data(0, Qt::UserRole+2).canConvert<QString>())
    {
        QString id = item->data(0, Qt::UserRole+2).toString();
        emit jobSelected(id);
    }
    else {
        std::cout << "Can't convert to jobT" << std::endl;
    }
}

void JobBoardWidget::addJobItem(QTreeWidgetItem *parent, JobT* job)
{
    QStringList data;
    data << QString::fromStdString(job->name)
         << QString::number(job->runCores)
         << QString::number(job->minCores)
         << QString::number(job->totals.waitingTaskCount)
         << QString::number(job->maxRssMb).append("MB");

    QTreeWidgetItem *jobItem = new QTreeWidgetItem((QTreeWidget*)0, data);
    jobItem->setData(0, Qt::UserRole+1, updateCounter);  
    jobItem->setData(0, Qt::UserRole+2, job->id.c_str());

    parent->addChild(jobItem);
    (*itemIndex)[job->id] = jobItem;
}

QTreeWidgetItem* JobBoardWidget::addFolderItem(FolderT* folder)
{
    QStringList data;
    data << QString::fromStdString(folder->name)
         << QString::number(folder->runCores)
         << QString::number(folder->minCores)
         << QString::number(folder->totals.waitingTaskCount);

    QTreeWidgetItem *folderItem = new QTreeWidgetItem((QTreeWidget*)0, data);
    folderItem->setData(0, Qt::UserRole+1, updateCounter);
    treeWidget->insertTopLevelItem(0, folderItem);
    (*itemIndex)[folder->id] = folderItem;

    return folderItem;
}

void JobBoardWidget::refresh()
{
    ItemIndex& items = *itemIndex;

    ProjectT project;
    getProjectByName(project, "test");
    getJobBoard(jobBoard, project);

    updateCounter++;

    for (std::vector<FolderT>::iterator i = jobBoard.begin();
                                        i != jobBoard.end();
                                        ++i)
    {

        QTreeWidgetItem *folderItem;
        if (items.find(i->id) != items.end())
        {
            
            folderItem = items[i->id];
            folderItem->setText(0, i->name.c_str());
            folderItem->setText(1, QString::number(i->runCores));
            folderItem->setText(2, QString::number(i->minCores));
            folderItem->setText(3, QString::number(i->totals.waitingTaskCount));
            folderItem->setData(0, Qt::UserRole+1, updateCounter);
        }
        else
        {
            folderItem = addFolderItem(&(*i));
        }
    
        for (std::vector<JobT>::iterator j = i->jobs.begin();
                                         j != i->jobs.end();
                                         j++)
        {
            if (items.find(j->id) != items.end())
            {
                
                QTreeWidgetItem *jobItem = items[j->id];
                jobItem->setText(1, QString::number(j->runCores));
                jobItem->setText(2, QString::number(j->minCores));
                jobItem->setText(3, QString::number(j->totals.waitingTaskCount));
                jobItem->setText(4, QString::number(j->maxRssMb).append("MB"));
                jobItem->setData(0, Qt::UserRole+1, updateCounter);       

                // do we need to delete the simpleProgress widget or is this handled by the treeWidget?
                // The desctructor does seems to get called
                // http://qt-project.org/forums/viewthread/6490
                //
                Plow::Gui::SimpleProgressBarWidget *simpleProgress = new SimpleProgressBarWidget();

                // pass a JobT object to the widget 
                simpleProgress->setJob((*j));

                QString toolTipString = QString("<b>Job: %1</b>\nSucceeded: %2\nFailed: %3")
                                                .arg(jobItem->text(0))
                                                .arg(j->totals.succeededTaskCount)
                                                .arg(j->totals.deadTaskCount);

                jobItem->setToolTip(4, toolTipString);
                treeWidget->setItemWidget(jobItem, 5, simpleProgress);
            }
            else
            {
                addJobItem(folderItem, &(*j));
            }
        }
    }

    // Delete un-updated widgets
    QTreeWidgetItemIterator it(treeWidget);
    while (*it) 
    {
        if ((*it)->data(0, Qt::UserRole+1) != updateCounter)
        {
            // TODO: fix to actually remove.
            // Remove from widget indexs
            (*it)->setHidden(true);
        }
        ++it;
    }

    treeWidget->expandAll();
}

void JobBoardWidget::load()
{
    ProjectT project;
    getProjectByName(project, "test");
    getJobBoard(jobBoard, project);

    for (std::vector<FolderT>::iterator i = jobBoard.begin();
                                        i != jobBoard.end();
                                        ++i)
    {
        QTreeWidgetItem *folderItem = addFolderItem(&(*i));
        for (std::vector<JobT>::iterator j = i->jobs.begin();
                                         j != i->jobs.end();
                                         j++)
        {
            addJobItem(folderItem, &(*j));
        }
    }

    treeWidget->expandAll();
}

} // Gui
} // Plow
