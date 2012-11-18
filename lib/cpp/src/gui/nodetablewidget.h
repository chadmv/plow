#ifndef PLOW_GUI_NODETABLEWIDGET_H_
#define PLOW_GUI_NODETABLEWIDGET_H_

#include <QWidget>
#include <QItemDelegate>

class QTableView;

namespace Plow {
namespace Gui {

class NodeModel;
class NodeProxyModel;

//
// NodeTableWidget
//
class NodeTableWidget
        : public QWidget
{
    Q_OBJECT

 public:
    explicit NodeTableWidget(QWidget *parent = 0);

    NodeModel* model() const;
    void setModel(NodeModel *aModel);

 private:
    QTableView* tableView;
    NodeProxyModel* proxyModel;
};

//
// ResourceDelegate
//
class ResourceDelegate : public QItemDelegate
{
    Q_OBJECT

 public:
    ResourceDelegate(int totalColumn, QObject *parent = 0);

    void paint(QPainter *painter,
               const QStyleOptionViewItem &option,
               const QModelIndex &index) const;

 private:
    int totalColumn;
};

}  // Gui
}  // Plow

#endif  // PLOW_GUI_NODETABLEWIDGET_H_

