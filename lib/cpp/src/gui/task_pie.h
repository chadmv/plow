#ifndef TASKPIEWIDGET_H
#define TASKPIEWIDGET_H

#include <QWidget>
#include "plow/plow.h"

namespace Plow {
namespace Gui {


class TaskPieWidget : public QWidget
{
    Q_OBJECT
    
public:

    TaskPieWidget(QWidget *parent = 0, Qt::WindowFlags = 0);
    ~TaskPieWidget();
    void setTasks(std::vector<TaskT> tasks);

private:
    int numberOfTasks_;
	std::vector<TaskT> m_tasks;


public slots:
    void handleJobSelected(const QString& id);

protected:
    void paintEvent(QPaintEvent * event);

};


}  // Gui
}  // Plow

#endif // TASKPIEWIDGET_H