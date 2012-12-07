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
    int w = this->width();
    int h = this->height();
    int leftMargin = 5;
    int rightMargin = 10;
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
    << QRectF (leftMargin, 0, (w-rightMargin)*waitingWidth, h)
    << QRectF (leftMargin, 0, (w-rightMargin)*runningWidth, h)
    << QRectF (leftMargin, 0, (w-rightMargin)*deadWith, h)
    << QRectF (leftMargin, 0, (w-rightMargin)*eatenWidth, h)
    << QRectF (leftMargin, 0, (w-rightMargin)*dependWidth, h)
    << QRectF (leftMargin, 0, (w-rightMargin)*succededWidth, h);

    QPainter painter;
    painter.begin(this);
    painter.setPen(Qt::NoPen);

    QPalette palette = QPalette();
    
    QBrush brush(palette.window().color().darker(), Qt::SolidPattern);
    
    // painter.setBrush(Qt::red);
    // painter.setBrush(brush);

    QRect bgFillRect = QRect(0, 0, w, h);
    painter.fillRect(bgFillRect, brush);

    // painter.setBrush(Qt::NoBrush);
    painter.setBrush(grad);

    int previousRightEdge = 0;
    for (int i = 0; i < rects.size(); ++i) 
    {    
        // std::cout << w << " " << rightMargin <<" " << w-rightMargin*succededWidth<< std::endl;
        grad.setColorAt(0, PlowStyle::TaskColors[i+1]);
        grad.setColorAt(1, PlowStyle::TaskColors[i+1].lighter());
        QRectF mRect = rects[i];
        // mRect.moveLeft(previousRightEdge);
        // rects[i].setRight(rects[i].width()-(2)+previousRightEdge);
        painter.fillRect(mRect, grad);
        // std::cout << rects[i].width() << " " << i <<  " " << rects[i].right(rects[i]) << " " << w  << std::endl;
        previousRightEdge = mRect.right()+5;
    }

    QPen pen = QPen(palette.buttonText().color());
    painter.setPen(pen);
    painter.setBrush(Qt::NoBrush);
    QRectF outline = QRectF(leftMargin, 0, w-rightMargin, h-2);
    painter.drawRect(outline);

    // QString progressString;
    // progressString.setNum(totalTasks-m_job.totals.waitingTaskCount);

    QString progressString = QString("Progress: %1\%")
                                                .arg((float)(totalTasks-m_job.totals.waitingTaskCount));


    painter.drawText(leftMargin+5, 0, 120, 200, 0, progressString, 0);

    painter.end();
}
}  // Gui
}  // Plow
