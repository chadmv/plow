#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>

#include "nodemodel.h"
#include "nodetablewidget.h"

namespace Plow {
namespace Gui {

NodeTableWidget::NodeTableWidget(QWidget *parent)
    : QWidget(parent)
{
    QVBoxLayout *layout = new QVBoxLayout;

    tableView = new QTableView(this);
    tableView->verticalHeader()->hide();
    tableView->setEditTriggers(tableView->NoEditTriggers);
    tableView->setSelectionBehavior(tableView->SelectRows);
    tableView->setSortingEnabled(true);
    tableView->sortByColumn(0, Qt::AscendingOrder);

    // default model
    tableView->setModel(new NodeModel(this));

    layout->addWidget(tableView);
    setLayout(layout);
}

QAbstractItemModel* NodeTableWidget::model() const {
    return tableView->model();
}

void NodeTableWidget::setModel(QAbstractItemModel* aModel) {
    tableView->setModel(aModel);
}
}  // Gui
}  // Plow
