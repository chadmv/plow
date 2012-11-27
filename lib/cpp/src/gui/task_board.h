#ifndef PLOW_GUI_TASK_BOARD_H
#define PLOW_GUI_TASK_BOARD_H

#include <vector>

#include <QAbstractTableModel>
#include <QVariant>
#include <QStringList>
#include <QComboBox>
#include <QLineEdit>
#include <QTableView>
#include <QString>
#include <QPushButton>

#include "plow/plow.h"

namespace Plow {
namespace Gui {

class TaskBoardModel: public QAbstractTableModel
{
    Q_OBJECT

public:
    TaskBoardModel(TaskFilterT* filter, QObject* parent = 0);
    ~TaskBoardModel();
    
    int rowCount(const QModelIndex & parent = QModelIndex()) const;
    int columnCount (const QModelIndex & parent = QModelIndex() ) const;
    QVariant data (const QModelIndex & index, int role = Qt::DisplayRole) const;

    static QStringList HeaderLabels;

private:
    std::vector<TaskT> m_tasks;


};

/*
*
*
*
*/
class TaskBoardView: public QTableView
{
    Q_OBJECT
public:
    TaskBoardView(QWidget* parent = 0);
    ~TaskBoardView();
};


/*
*
*
*
*
*/
class TaskBoardWidget: public QWidget
{
    Q_OBJECT
public:
    TaskBoardWidget(QWidget* widget = 0);
    ~TaskBoardWidget();

    void setJob(const JobT& job);
    TaskFilterT* getTaskFilter() const;

public slots:
    void handleJobSelected(const QString& id);

private:
    TaskFilterT* m_filter;
    QPushButton* m_layers;
    QPushButton* m_states;
    QLineEdit* m_range;
    TaskBoardView* m_view;
};

} // 
} //

#endif