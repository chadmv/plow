#ifndef NODETABLEWIDGET_H
#define NODETABLEWIDGET_H

#include <QWidget>

class QTableView;

namespace Plow { namespace Gui {

class NodeModel;

class NodeTableWidget : public QWidget
{
    Q_OBJECT
public:
    NodeTableWidget(QWidget *parent = 0);
    NodeModel* model();
    void setModel(NodeModel*);
    
signals:
    
public slots:

private:
    QTableView* tableView;

};

} // Gui
} // Plow

#endif // NODETABLEWIDGET_H
