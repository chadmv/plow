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
    
}


TaskPieWidget::~TaskPieWidget()
{
    
}


void TaskPieWidget::setTasks(std::vector<TaskT> tasks)
{
    m_tasks = tasks;
    update();
}

void TaskPieWidget::setJob(JobT job)
{
    m_job = job;
    update();
}


void TaskPieWidget::handleJobSelected(const QString& id)
{
    JobT job;
    getJobById(job, id.toStdString());
    setJob(job);

    TaskFilterT filter;
    filter.jobId = job.id;

    std::vector<TaskT> tasks;
    getTasks(tasks, filter);
    setTasks(tasks);

    std::vector<LayerT> layers;
    getLayers(layers, job);
    // for (int r = 0; r < layers.size(); ++r)
    // {
        
        // QString layerName = QString::fromStdString(layers[r].name);
        // std::cout << layers[r].name << std::endl;
    // }    
    

    // setJob(job);
}


void TaskPieWidget::paintEvent(QPaintEvent * event)
{

    QPainter painter;
    QPalette palette = QPalette();
    int w = this->width();
    int h = this->height();

    float spanAngle = 5760;
    if (m_tasks.size() > 0)
    {       
        spanAngle = 5760.0/m_tasks.size();
    }


    painter.begin(this);
    painter.setRenderHint(QPainter::Antialiasing);

    // work out radius
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
    QBrush brush(Qt::lightGray, Qt::SolidPattern);
    QPen pen = QPen(Qt::NoPen);
   
    for (std::vector<TaskT>::iterator i = m_tasks.begin();
                                      i != m_tasks.end();
                                      ++i)
    {
        painter.setBrush(Qt::gray);
        QColor sliceColor = QColor(PlowStyle::TaskColors[i->state]);

        painter.setPen(sliceColor);
        painter.setBrush(sliceColor);   

        int index = i-m_tasks.begin();
        painter.drawPie(rectangle, index*spanAngle, spanAngle);

    }

    // outline
    pen = QPen(palette.buttonText().color());
    painter.setPen(pen);
    painter.setBrush(Qt::NoBrush);
    painter.drawEllipse(rectangle);
    
    // status text
    QString wait;
    QString running;
    QString dead;
    QString eaten;
    QString depend;
    QString succeed;

    QList<QString> statusText = QList<QString>()
    << wait.sprintf("w: %i", m_job.totals.waitingTaskCount)
    << running.sprintf("r: %i", m_job.totals.runningTaskCount)
    << dead.sprintf("d: %i", m_job.totals.deadTaskCount)
    << eaten.sprintf("e: %i", m_job.totals.eatenTaskCount)
    << depend.sprintf("d: %i", m_job.totals.dependTaskCount)
    << succeed.sprintf("s: %i", m_job.totals.succeededTaskCount);

    pen = QPen(palette.buttonText().color());
    painter.setPen(pen);

    QFont font = painter.font();
    font.setFamily("Monospace");
    painter.setFont(font);

    const int x_padding = 5;
    const int y_padding = 15;
    for (int i = 0; i < statusText.size(); ++i) 
    { 
        painter.drawText(x_padding, y_padding*(i+1), statusText[i]);
    }
   
    // job name
    QString jobName = m_job.name.c_str();
    QFontMetrics metric = painter.fontMetrics();
    int name_width = metric.width(jobName);
    painter.drawText((w/2)-(name_width/2), h-10, jobName);
    

    painter.end();
}

}  // Gui

}  // Plow

