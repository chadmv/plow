#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>

#include "nodemodel.h"
#include "nodetablewidget.h"

namespace Plow {
namespace Gui {

//
// NodeTableWidget
//
NodeTableWidget::NodeTableWidget(QWidget *parent)
    : QWidget(parent)
{
    QVBoxLayout *layout = new QVBoxLayout(this);

    // default model
    proxyModel = new NodeProxyModel(this);
    proxyModel->setSourceModel(new NodeModel(this));

    tableView = new QTableView(this);
    tableView->verticalHeader()->hide();
    tableView->setEditTriggers(tableView->NoEditTriggers);
    tableView->setSelectionBehavior(tableView->SelectRows);
    tableView->setSortingEnabled(true);

    tableView->sortByColumn(0, Qt::AscendingOrder);
    tableView->setModel(proxyModel);

    tableView->setColumnHidden(8, true); // total RAM
    tableView->setColumnHidden(10, true); // total SWAP

    layout->addWidget(tableView);

    // Map free ram to total ram
    tableView->setItemDelegateForColumn(9, new ResourceDelegate(8, this));
    // Map free swap to total swap
    tableView->setItemDelegateForColumn(11, new ResourceDelegate(10, this));
}

NodeModel* NodeTableWidget::model() const {
    return qobject_cast<NodeModel*>(proxyModel->sourceModel());
}

void NodeTableWidget::setModel(NodeModel *aModel) {
    proxyModel->setSourceModel(aModel);
}

//
// ResourceDelegate
//
ResourceDelegate::ResourceDelegate(int totalColumn, QObject *parent)
    : QItemDelegate(parent)
{
    this->totalColumn = totalColumn;
}

void ResourceDelegate::paint(QPainter *painter,
                             const QStyleOptionViewItem &option,
                             const QModelIndex &index) const
{
    QVariant totalData = index.model()->index(index.row(), totalColumn).data();
    QVariant currentData = index.data();
    if (totalData.canConvert<double>()
            && currentData.canConvert<double>()) {

        QString text = QString("%1%")
                .arg((currentData.toDouble() / totalData.toDouble()) * 100,
                     5, 'f', 1);

        QStyleOptionViewItem opt = option;
        opt.displayAlignment = Qt::AlignCenter;

        drawDisplay(painter, opt, opt.rect, text);

    } else {
        QItemDelegate::paint(painter, option, index);
    }
}

}  // Gui
}  // Plow
