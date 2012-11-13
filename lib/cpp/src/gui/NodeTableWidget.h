#ifndef PLOW_GUI_NODETABLEWIDGET_H_
#define PLOW_GUI_NODETABLEWIDGET_H_

#include <QWidget>

class QTableView;


namespace Plow { namespace Gui {

class NodeModel;

class NodeTableWidget
        : public QWidget
{
    Q_OBJECT

public:
    explicit NodeTableWidget(QWidget *parent = 0);

    NodeModel* model();
    void setModel(NodeModel* nodeModel);

private:
    QTableView* tableView;
};
}  // Gui
}  // Plow

#endif  // PLOW_GUI_NODETABLEWIDGET_H_
