#ifndef PLOW_DOCK_WIDGETS_H
#define PLOW_DOCK_WIDGETS_H

#include <QDockWidget>

#include "job_board.h"
#include "task_board.h"
#include "nodetablewidget.h"
#include "event.h"

namespace Plow {
namespace Gui {

class JobBoardDockWidget: public QDockWidget
{
    Q_OBJECT

public:
    JobBoardDockWidget(JobBoardWidget* boardWidget, QWidget* parent=0);

};

class TaskBoardDockWidget: public QDockWidget
{
    Q_OBJECT

public:
    TaskBoardDockWidget(TaskBoardWidget* boardWidget, QWidget* parent=0);

};

class NodeTableDockWidget: public QDockWidget
{
    Q_OBJECT

public:
    NodeTableDockWidget(NodeTableWidget* nodeWidget, QWidget* parent=0);

};

} //
} //

#endif
