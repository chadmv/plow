#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>
#include <QPainter>

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

        double total = totalData.toDouble();
        double current = currentData.toDouble();
        double ratio = current / total;

        QString text = QString("%1%").arg(ratio * 100, 5, 'f', 1);

        QStyleOptionViewItem opt = option;
        opt.displayAlignment = Qt::AlignRight|Qt::AlignVCenter;

        QLinearGradient grad(opt.rect.topLeft(), opt.rect.topRight());
        QColor darkGreen = QColor(42,175,32);
        QColor darkEnd = Qt::white;
        QColor end = Qt::white;

        if (ratio <= .05) {  // 5% ram warning
            darkEnd = QColor(255,0,0,.5);
            end = Qt::red;
        } else if (ratio <= .15) {  // %15 ram warning
            darkEnd = QColor(197,203,37,.5);
            end = Qt::yellow;
        }
        grad.setColorAt(0.0, darkGreen);
        grad.setColorAt(ratio, Qt::green);
        grad.setColorAt(std::min(ratio + .01, 1.0), end);
        grad.setColorAt(1.0, darkEnd);

        painter->fillRect(opt.rect, QBrush(grad));
        drawDisplay(painter, opt, opt.rect, text);

    } else {
        QItemDelegate::paint(painter, option, index);
    }
}

}  // Gui
}  // Plow
