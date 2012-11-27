#include <vector>

#include <QHBoxLayout>
#include <QVBoxLayout>
#include <QString>
#include <QStandardItemModel>
#include <QAction>
#include <QMenu>
#include <QHeaderView>


#include "common.h"
#include "event.h"
#include "task_board.h"

namespace Plow {
namespace Gui {

QStringList TaskBoardModel::HeaderLabels = QStringList()
    << "Name"
    << "State"
    << "Node"
    << "Duration";


/* TaskBoardWidget
**------------------------------------------------*/

TaskBoardWidget::TaskBoardWidget(QWidget* parent) :
    QWidget(parent),
    m_filter(new TaskFilterT()),
    m_layers(new QPushButton(this)),
    m_states(new QPushButton(this)),
    m_range(new QLineEdit(this)),
    m_view(new TaskBoardView(this))
{
    m_layers->setText("Layers");
    m_layers->setMenu(new QMenu(this));

    m_states->setText("States");
    m_states->setMenu(new QMenu(this));

    m_view->horizontalHeader()->setStretchLastSection(true);
 
    std::map<int, const char*>::const_iterator iter;
    for (iter = _TaskState_VALUES_TO_NAMES.begin();
         iter != _TaskState_VALUES_TO_NAMES.end(); ++iter)
    {
        QAction* action = new QAction(
            QString(iter->second), m_states->menu());
        action->setData(iter->first);
        action->setCheckable(true);
        m_states->menu()->addAction(action);
    }
    
    QHBoxLayout* ctl_layout = new QHBoxLayout();
    ctl_layout->addWidget(m_layers);
    ctl_layout->addWidget(m_states);
    ctl_layout->addWidget(m_range);

    QVBoxLayout* layout = new QVBoxLayout(this);
    layout->addLayout(ctl_layout);
    layout->addWidget(m_view);

    connect(EventManager::getInstance(), SIGNAL(jobSelected(QString)),
        this, SLOT(handleJobSelected(QString)));
}

TaskBoardWidget::~TaskBoardWidget()
{

}

void TaskBoardWidget::handleJobSelected(const QString& id)
{
    JobT job;
    getJobById(job, id.toStdString());
    setJob(job);
}

void  TaskBoardWidget::setJob(const JobT& job)
{
    m_filter->jobId = job.id;

    std::vector<LayerT> layers;
    Plow::getLayers(layers, job);

    m_layers->menu()->clear();
    for (int r = 0; r < layers.size(); ++r)
    {
        QAction* action = new QAction(
            QString::fromStdString(layers[r].name), m_layers->menu());
        action->setCheckable(true);
        m_layers->menu()->addAction(action);
    }

    TaskBoardModel *model = new TaskBoardModel(m_filter, this);
    delete m_view->model();
    m_view->setModel(model);
    m_view->setColumnWidth(0, 500);

}

TaskFilterT* TaskBoardWidget::getTaskFilter() const
{
    return m_filter;
}


/* TaskBoardView
**------------------------------------------------*/

TaskBoardView::TaskBoardView(QWidget* parent) :
    QTableView(parent)
{


}

TaskBoardView::~TaskBoardView()
{

}

/* TaskBoardModel
**------------------------------------------------*/


TaskBoardModel::TaskBoardModel(TaskFilterT* filter, QObject* parent) : 
    QAbstractTableModel(parent)
{
    getTasks(m_tasks, *filter);
    reset();
}

TaskBoardModel::~TaskBoardModel()
{

}

int TaskBoardModel::rowCount(const QModelIndex& parent) const
{
    return m_tasks.size();
}

int TaskBoardModel::columnCount (const QModelIndex& parent) const
{
    return TaskBoardModel::HeaderLabels.size();
}

QVariant TaskBoardModel::data (const QModelIndex & index, int role) const
{
    int row = index.row();
    int col = index.column();

    const TaskT& task = m_tasks[row];

    switch(role)
    {

    case Qt::DisplayRole:
        if (col == 0)
        {
            return QString(task.name.c_str());
        }
        else if (col == 1)
        {
            int state = task.state;
            return QString(_TaskState_VALUES_TO_NAMES.find(state)->second);
        }
        else if (col == 2)
        {
            return QString(task.lastNodeName.c_str());
        }
        else if (col == 3)
        {
            std::string duration;
            formatDuration(duration, task.startTime, task.stopTime);
            return QString(duration.c_str());
        }
        break;

    case Qt::BackgroundRole:
        if (col == 1)
        {
            int state = task.state;
            return PlowStyle::TaskColors[state];
        }
        break;
    }

    return QVariant();
}

} //
} //