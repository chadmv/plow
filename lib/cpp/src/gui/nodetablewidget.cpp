#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>
#include <QPainter>
#include <QDebug>

#include "common.h"
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
    NodeModel *model = new NodeModel(this);
    proxyModel = new NodeProxyModel(this);
    proxyModel->setSourceModel(model);

    tableView = new QTableView(this);
    tableView->verticalHeader()->hide();
    tableView->setEditTriggers(tableView->NoEditTriggers);
    tableView->setSelectionBehavior(tableView->SelectRows);
    tableView->setSortingEnabled(true);

    tableView->sortByColumn(0, Qt::AscendingOrder);
    tableView->setModel(proxyModel);

    layout->addWidget(tableView);

    int col_ram_total = model->indexOfHeaderName("Ram (Total)");
    int col_ram_free = model->indexOfHeaderName("Ram (Free)");
    int col_swap_total = model->indexOfHeaderName("Swap (Total)");
    int col_swap_free = model->indexOfHeaderName("Swap (Free)");

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
{
    COLOR_CRITICAL = PlowStyle::TaskColors.at(TaskState::DEAD);
    COLOR_WARN = PlowStyle::TaskColors.at(TaskState::RUNNING).lighter(115);
    COLOR_OK = PlowStyle::TaskColors.at(TaskState::SUCCEEDED).lighter(115);
}

void ResourceDelegate::paint(QPainter *painter,
                             const QStyleOptionViewItem &option,
                             const QModelIndex &index) const
{
    QVariant currentData = index.data(Qt::UserRole);
    if (currentData.canConvert<double>()) {
        double ratio = currentData.toDouble();

        QString text = QString("%1%").arg(ratio * 100, 5, 'f', 2);

        QStyleOptionViewItemV4 opt = option;
        opt.displayAlignment = Qt::AlignRight|Qt::AlignVCenter;

        QLinearGradient grad(opt.rect.topLeft(), opt.rect.topRight());
        QColor darkEnd = Qt::transparent;
        QColor end = darkEnd;

        if (ratio == 1.0) {
            darkEnd = COLOR_OK;
            end = darkEnd;

        } else if (ratio <= .05) {  // 5% ram warning
            darkEnd = COLOR_CRITICAL.darker(135);
            end = COLOR_CRITICAL;

        } else if (ratio <= .15) {  // %15 ram warning
            darkEnd = COLOR_WARN.darker(135);
            end = COLOR_WARN;
        }
        grad.setColorAt(0.0, COLOR_OK.darker(135));
        grad.setColorAt(ratio, COLOR_OK);
        grad.setColorAt(qMin(ratio + .01, 1.0), end);
        grad.setColorAt(1.0, darkEnd);

        drawBackground(painter, opt, index);
        painter->fillRect(opt.rect, QBrush(grad));
        drawDisplay(painter, opt, opt.rect, text);

    } else {
        QItemDelegate::paint(painter, option, index);
    }
}

}  // Gui
}  // Plow
