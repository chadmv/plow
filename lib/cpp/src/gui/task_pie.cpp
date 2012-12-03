#include <QPainter>
#include <QAction>

#include "task_pie.h"
#include "event.h"

namespace Plow {
namespace Gui {


TaskPieWidget::TaskPieWidget(QWidget *parent,  Qt::WindowFlags flag)
    : QWidget(parent)
{
    
    setGeometry(150, 150, 400, 400);
    setContextMenuPolicy(Qt::ActionsContextMenu);

    connect(EventManager::getInstance(), SIGNAL(jobSelected(QString)),
        this, SLOT(handleJobSelected(QString)));


    numberOfTasks_ = 0;

}


TaskPieWidget::~TaskPieWidget()
{
    
}


void TaskPieWidget::setTasks(std::vector<TaskT> tasks)
{
    m_tasks = tasks;
    update();
}

void TaskPieWidget::handleJobSelected(const QString& id)
{
    JobT job;
    getJobById(job, id.toStdString());
    
    // int totalTasks = m_tasks.totals.totalTaskCount;
    // std::cout << id.toStdString() << " " << totalTasks << std::endl;

    TaskFilterT filter;
    filter.jobId = job.id;

    std::vector<TaskT> tasks;
    getTasks(tasks, filter);
    setTasks(tasks);

    // setJob(job);
}


void TaskPieWidget::paintEvent(QPaintEvent * event)
{

    QPainter p;
    QBrush brush(Qt::lightGray, Qt::SolidPattern);

    int w = this->width();
    int h = this->height();

    QLinearGradient grad(w/2, h, w/2, 0);
    grad.setColorAt(0, Qt::darkGray);
    grad.setColorAt(1, Qt::white);

    float spanAngle = 5760/10;
    if (m_tasks.size() > 0)
    {       
        spanAngle = 5760/m_tasks.size();
    }


    p.begin(this);
    QPen pen = p.pen();
    // QPen pen = QPen(Qt::NoPen);
    pen.setColor(QColor(160, 159, 155, 75));
    pen.setWidth(0.541);
    p.setPen(pen);

    p.setBrush(brush);
    p.setRenderHint(QPainter::Antialiasing);

    QRectF rectangle(2, 2, w-2, h-2);
  
    //   enum TaskState {
    //     INITIALIZE,
    //     WAITING,
    //     RUNNING,
    //     DEAD,
    //     EATEN,
    //     DEPEND,
    //     SUCCEEDED
    // }

    for (std::vector<TaskT>::iterator i = m_tasks.begin();
                                      i != m_tasks.end();
                                      ++i)
    {
        // std::cout << i->progress << std::endl;
        
        p.setBrush(Qt::gray);
        if (i->state == 3 || i->state == 4) // dead, eaten
        {
            p.setBrush(QColor(167, 20, 14).darker(150));
        }
        else if (i->state == 1) // waiting
        {
            p.setBrush(Qt::darkGray);   
        }
        else if (i->state == 2) // running
        {
            p.setBrush(QColor(167, 159, 22).darker(150));   
        }        
        else if (i->state == 6) // succeeded
        {
            p.setBrush(Qt::green);   
        }
        int index = i-m_tasks.begin();
        p.drawPie(rectangle, index*spanAngle, spanAngle);
        // std::cout << i->state << std::endl;

    }


    // p.drawText(0, 0, 120, 200, 0, tr("My text"), 0);
    p.end();
}

}  // Gui

}  // Plow

