#include <vector>

#include <QHBoxLayout>
#include <QVBoxLayout>
#include <QString>
#include <QStandardItemModel>
#include <QAction>
#include <QMenu>
#include <QHeaderView>
#include <QTimer>

#include "common.h"
#include "event.h"
#include "task_board.h"

namespace Plow {
namespace Gui {

QStringList TaskBoardModel::HeaderLabels = QStringList()
    << "Name"
    << "State"
    << "Node"
    << "Duration"
    << "Log";

const int COLUMNS = 5;

/* TaskBoardWidget
**------------------------------------------------*/

TaskBoardWidget::TaskBoardWidget(QWidget* parent) :
    QWidget(parent),
    m_filter(new TaskFilterT()),
    m_layers(new QPushButton(this)),
    m_states(new QPushButton(this)),
    m_job_name(new QLabel(this)),
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
    ctl_layout->addStretch();
    ctl_layout->addWidget(m_job_name);

    QVBoxLayout* layout = new QVBoxLayout(this);
    layout->addLayout(ctl_layout);
    layout->addWidget(m_view);

    connect(EventManager::getInstance(), SIGNAL(jobSelected(QString)),
        this, SLOT(handleJobSelected(QString)));

    m_model = 0;
    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(refreshModel()));
}

TaskBoardWidget::~TaskBoardWidget()
{
    delete m_timer;
}

void TaskBoardWidget::refreshModel()
{
    if (m_model)
    {
        m_model->refresh();
    }
}

void TaskBoardWidget::handleJobSelected(const QString& id)
{
    JobT job;
    getJobById(job, id.toStdString());
    setJob(job);
}

void  TaskBoardWidget::setJob(const JobT& job)
{
    // Stop the timer
    m_timer->stop();

    std::vector<LayerT> layers;
    Plow::getLayers(layers, job);

    // Update the layer display
    m_layers->menu()->clear();
    for (int r = 0; r < layers.size(); ++r)
    {
        QAction* action = new QAction(
            QString::fromStdString(layers[r].name), m_layers->menu());
        action->setCheckable(true);
        m_layers->menu()->addAction(action);
    }

    // Set up a new model
    m_filter->jobId = job.id;
    TaskBoardModel* model = new TaskBoardModel(*m_filter, this);
    model->init();
    
    m_job_name->setText(QString::fromStdString(job.name));
    m_view->setModel(model);
    m_view->setColumnWidth(0, 500);

    // If we have an old model set, delete it.
    if (m_model) {
        delete m_model;
        m_model = 0;
    }

    // Set the model and restart the timer.
    m_model = model;
    m_timer->start(3000);
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

    connect(this, SIGNAL(doubleClicked(const QModelIndex&)),
        this, SLOT(taskDoubleClickedHandler(const QModelIndex&)));
}

TaskBoardView::~TaskBoardView()
{

}

void TaskBoardView::taskDoubleClickedHandler(const QModelIndex& index) 
{
    QString taskId = model()->data(index, Qt::UserRole).toString();
    //TODO: add log file viewer.
}

/* TaskBoardModel
**------------------------------------------------*/


TaskBoardModel::TaskBoardModel(TaskFilterT filter, QObject* parent) : 
    QAbstractTableModel(parent),
    m_filter(filter)
{

}

TaskBoardModel::~TaskBoardModel()
{
    m_timer->stop();
    delete m_timer;
}

void TaskBoardModel::init()
{
    uint64_t qtime = getPlowTime();

    getTasks(m_tasks, m_filter);
    m_filter.lastUpdateTime = qtime;

    for(int i=0; i<m_tasks.size(); i++) {
        m_index[m_tasks[i].id] = i;
    }
    
    m_timer = new QTimer(this);
    connect(m_timer, SIGNAL(timeout()), this, SLOT(refreshRunningTime()));
    m_timer->start(1000);
}

void TaskBoardModel::refresh()
{

    // Get the time from the server.
    uint64_t plow_time = getPlowTime();

    // Make a query to get the tasks since the last update time.
    std::vector<TaskT> updated;
    getTasks(updated, m_filter);

    // Set the update time for the next query;
    m_filter.lastUpdateTime = plow_time;

    std::vector<TaskT>::iterator iter;
    for (iter = updated.begin(); iter != updated.end(); ++iter)
    {
        int idx = m_index[iter->id];
        m_tasks[idx] = *iter;
        emit dataChanged(index(idx, 0), index(idx, COLUMNS-1));
    }
}

void TaskBoardModel::refreshRunningTime()
{
    std::vector<TaskT>::iterator iter;
    for (iter = m_tasks.begin(); iter != m_tasks.end(); ++iter)
    {
        if (iter->state == TaskState::RUNNING)
        {
            emit dataChanged(index(m_index[iter->id], 0),
                             index(m_index[iter->id], COLUMNS-1));
        }
    }
}

int TaskBoardModel::rowCount(const QModelIndex& parent) const
{
    return m_tasks.size();
}

int TaskBoardModel::columnCount (const QModelIndex& parent) const
{
    return COLUMNS;
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
            return QString::fromStdString(duration);
        }
        else if (col == 4) {
            return QString(task.lastLogLine.c_str());
        }
        break;

    case Qt::BackgroundRole:
        if (col == 1)
        {
            int state = task.state;
            return PlowStyle::TaskColors[state];
        }
        break;

    case Qt::UserRole:
        return QString::fromStdString(task.id);

    }

    return QVariant();
}

} //
} //