#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>
#include <QPainter>
#include <QDebug>

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
    NodeModel model;
    proxyModel = new NodeProxyModel(this);
    proxyModel->setSourceModel(&model);

    tableView = new QTableView(this);
    tableView->verticalHeader()->hide();
    tableView->setEditTriggers(tableView->NoEditTriggers);
    tableView->setSelectionBehavior(tableView->SelectRows);
    tableView->setSortingEnabled(true);

    tableView->sortByColumn(0, Qt::AscendingOrder);
    tableView->setModel(proxyModel);

    layout->addWidget(tableView);

    int col_ram_total = model.indexOfHeaderName("Ram (Total)");
    int col_ram_free = model.indexOfHeaderName("Ram (Free)");
    int col_swap_total = model.indexOfHeaderName("Swap (Total)");
    int col_swap_free = model.indexOfHeaderName("Swap (Free)");

    tableView->setColumnHidden(col_ram_total, true);
    tableView->setColumnHidden(col_swap_total, true);

    tableView->setItemDelegateForColumn(
                col_ram_free,
                new ResourceDelegate(this));

    tableView->setItemDelegateForColumn(
                col_swap_free,
                new ResourceDelegate(this));
}


//
// ResourceDelegate
//
ResourceDelegate::ResourceDelegate(QObject *parent)
    : QItemDelegate(parent)
{}

void ResourceDelegate::paint(QPainter *painter,
                             const QStyleOptionViewItem &option,
                             const QModelIndex &index) const
{
    QVariant currentData = index.data(Qt::UserRole);
    if (currentData.canConvert<double>()) {
        double ratio = currentData.toDouble();

        QString text = QString("%1%").arg(ratio * 100, 5, 'f', 2);

        QStyleOptionViewItem opt = option;
        opt.displayAlignment = Qt::AlignRight|Qt::AlignVCenter;

        QLinearGradient grad(opt.rect.topLeft(), opt.rect.topRight());
        QColor darkGreen = QColor(42,175,32);
        QColor darkEnd = Qt::white;
        QColor end = darkEnd;

        if (ratio == 1.0) {
            darkEnd = Qt::green;
            end = darkEnd;
        } else if (ratio <= .05) {  // 5% ram warning
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
