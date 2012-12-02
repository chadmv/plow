#include "simple_progressbar_widget.h"
#include <QPainter>
#include "plow/plow.h"
#include "common.h"
namespace Plow {
namespace Gui {



SimpleProgressBarWidget::SimpleProgressBarWidget(QWidget *parent)
    : QWidget(parent)
{
}

SimpleProgressBarWidget::~SimpleProgressBarWidget()
{
    // qDebug("Destruct!");
}

void SimpleProgressBarWidget::setJob(JobT job)
{
    m_job = job;
}


void SimpleProgressBarWidget::paintEvent(QPaintEvent * event)
{


    QPainter painter;
    QBrush brush(Qt::lightGray, Qt::SolidPattern);

    int w = this->width();
    int h = this->height();
    int leftMargin = 5;
    int rightMargin = 10;
    w = w-rightMargin;

    int totalTasks = m_job.totals.totalTaskCount;

    float waitingWidth = (float)m_job.totals.waitingTaskCount/(float)totalTasks;
    float runningWidth = (float)m_job.totals.runningTaskCount/(float)totalTasks;
    float deadWith = (float)m_job.totals.deadTaskCount/(float)totalTasks;
    float eatenWidth = (float)m_job.totals.eatenTaskCount/(float)totalTasks;
    float dependWidth = (float)m_job.totals.dependTaskCount/(float)totalTasks;
    float succededWidth = (float)m_job.totals.succeededTaskCount/(float)totalTasks;

    // float waitingWidth = 0.16666;
    // float runningWidth = 0.16666;
    // float deadWith = 0.16666;
    // float eatenWidth = 0.16666;
    // float dependWidth = 0.16666;
    // float succededWidth = 0.16666;

    QLinearGradient grad(w/2, h, w/2, 0);
    grad.setColorAt(0, Qt::darkBlue);
    grad.setColorAt(1, Qt::white);

    QList<QRectF> rects = QList<QRectF>()
    << QRectF (0, 0, w*succededWidth, h)
    << QRectF (0, 0, w*runningWidth, h)
    << QRectF (0, 0, w*deadWith, h)
    << QRectF (0, 0, w*eatenWidth, h)
    << QRectF (0, 0, w*dependWidth, h)
    << QRectF (0, 0, w*succededWidth, h);
    
    painter.begin(this);

    float previousRightEdge = leftMargin;
    for (int i = 0; i < rects.size(); ++i) 
    {    
        grad.setColorAt(0, PlowStyle::TaskColors[i+1]);
        grad.setColorAt(1, PlowStyle::TaskColors[i+1].lighter());
        rects[i].moveLeft(previousRightEdge);
        painter.fillRect(rects[i], grad);
        previousRightEdge = rects[i].right();
    }

    QPalette palette = QPalette();
    QPen pen = QPen(palette.buttonText().color());
    painter.setPen(pen);
    QRect border = QRect(leftMargin, 0, w, h-2);
    painter.drawRect(border);

    QString progressString;
    progressString.setNum(m_job.totals.waitingTaskCount);
    painter.drawText(leftMargin+5, 0, 120, 200, 0, progressString, 0);

    painter.end();
}
}  // Gui
}  // Plow
