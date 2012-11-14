#ifndef PLOW_GUI_NODETABLEWIDGET_H_
#define PLOW_GUI_NODETABLEWIDGET_H_

#include <QWidget>

class QTableView;
class QAbstractItemModel;

namespace Plow {
namespace Gui {

class NodeTableWidget
        : public QWidget
{
    Q_OBJECT

 public:
    explicit NodeTableWidget(QWidget *parent = 0);

    QAbstractItemModel* model() const;
    void setModel(QAbstractItemModel* aModel);

 private:
    QTableView* tableView;
};
}  // Gui
}  // Plow

#endif  // PLOW_GUI_NODETABLEWIDGET_H_
