#include <QPainter>
#include <QAction>

#include "task_pie.h"
#include "event.h"
#include "common.h"

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
    

    int w = this->width();
    int h = this->height();

    // QLinearGradient grad(w/2, h, w/2, 0);
    // grad.setColorAt(0, Qt::darkGray);
    // grad.setColorAt(1, Qt::white);

    float spanAngle = 576.0;
    if (m_tasks.size() > 0)
    {       
        spanAngle = 5760.0/m_tasks.size();
        // qDebug("%f", spanAngle);
    }


    p.begin(this);
    // QPen pen = p.pen();
    
    p.setRenderHint(QPainter::Antialiasing);

    int padding = 50;
    int pieDiameter = w-padding;
    int pieHoriz = (w-pieDiameter)/2;
    int pieVert = (h-pieDiameter)/2;
    if ((w+padding) > (h+padding)) 
    {
        pieDiameter = h-padding;
        pieHoriz = (w-pieDiameter)/2;
        pieVert = (h-pieDiameter)/2;
    }
    
    QRectF rectangle(pieHoriz, pieVert, pieDiameter, pieDiameter);
    
    p.setBrush(Qt::NoBrush);
    // p.drawRect(rectangle);
    
    QBrush brush(Qt::lightGray, Qt::SolidPattern);
    p.setBrush(brush);
    QPen pen = QPen(Qt::NoPen);
    pen.setColor(QColor(160, 159, 155, 75));
    pen.setWidth(0.541);
    p.setPen(pen);

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
        QColor sliceColor = QColor(PlowStyle::TaskColors[i->state]);
        

        p.setPen(sliceColor);
        p.setBrush(sliceColor);   

        int index = i-m_tasks.begin();
        p.drawPie(rectangle, index*spanAngle, spanAngle);
        // std::cout << i->state << std::endl;

    }


    // p.drawText(0, 0, 120, 200, 0, tr("My text"), 0);
    p.end();
}

}  // Gui

}  // Plow

