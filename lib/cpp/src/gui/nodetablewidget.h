#ifndef PLOW_GUI_NODETABLEWIDGET_H_
#define PLOW_GUI_NODETABLEWIDGET_H_

#include <QWidget>
#include <QItemDelegate>
#include "nodemodel.h"

class QTableView;

namespace Plow {
namespace Gui {

//
// NodeTableWidget
//
class NodeTableWidget
        : public QWidget
{
    Q_OBJECT

 public:
    explicit NodeTableWidget(QWidget *parent = 0);

    inline NodeModel* model() const
    { return qobject_cast<NodeModel*>(proxyModel->sourceModel()); }

    inline void setModel(NodeModel *aModel)
    { proxyModel->setSourceModel(aModel); }

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
    ResourceDelegate(QObject *parent = 0);

    void paint(QPainter *painter,
               const QStyleOptionViewItem &option,
               const QModelIndex &index) const;

};

}  // Gui
}  // Plow

#endif  // PLOW_GUI_NODETABLEWIDGET_H_

