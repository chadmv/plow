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
#include <QTimer>
#include <QLabel>
#include <QModelIndex>

#include "plow/plow.h"

namespace Plow {
namespace Gui {

class TaskBoardModel: public QAbstractTableModel
{
    Q_OBJECT

public:
    TaskBoardModel(TaskFilterT filter, QObject* parent = 0);
    ~TaskBoardModel();
    
    int rowCount(const QModelIndex & parent = QModelIndex()) const;
    int columnCount (const QModelIndex & parent = QModelIndex() ) const;
    QVariant data (const QModelIndex & index, int role = Qt::DisplayRole) const;
    void init();
    static QStringList HeaderLabels;

public slots:
    void refresh();

private slots:
    void refreshRunningTime();

private:
    std::vector<TaskT> m_tasks;
    std::map<Guid, int> m_index;
    uint64_t m_time;
    TaskFilterT m_filter;
    QTimer *m_timer;
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

public slots:
    void taskDoubleClickedHandler(const QModelIndex& index);
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

private slots:
    void refreshModel();

private:
    TaskFilterT* m_filter;
    QPushButton* m_layers;
    QPushButton* m_states;
    QLabel * m_job_name;
    TaskBoardView* m_view;
    TaskBoardModel* m_model;
    QTimer *m_timer;
};

} // 
} //

#endif