#ifndef PLOW_GUI_SIMPLEPROGRESSBARWIDGET_H_
#define PLOW_GUI_SIMPLEPROGRESSBARWIDGET_H_

#include <QWidget>
#include "plow/plow.h"

namespace Plow {
namespace Gui {

class SimpleProgressBarWidget : public QWidget
{
    Q_OBJECT
    
public:
    SimpleProgressBarWidget(QWidget *parent = 0);
    ~SimpleProgressBarWidget();
    void setJob(JobT job);


private:
    JobT m_job;
    
protected:
    void paintEvent(QPaintEvent * event);

};


}  // Gui
}  // Plow

#endif // WIDGET_H