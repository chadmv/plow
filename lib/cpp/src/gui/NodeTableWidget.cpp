#include <QHeaderView>
#include <QVBoxLayout>
#include <QTableView>
#include <QPushButton>

#include "NodeModel.h"
#include "NodeTableWidget.h"

NodeTableWidget::NodeTableWidget(QWidget *parent) :
    QWidget(parent)
{
    QVBoxLayout *layout = new QVBoxLayout;

    tableView = new QTableView;
    tableView->verticalHeader()->hide();
    tableView->setEditTriggers(tableView->NoEditTriggers);
    tableView->setSelectionBehavior(tableView->SelectRows);
    tableView->setSortingEnabled(true);

    // default model
    tableView->setModel(new NodeModel);

    layout->addWidget(tableView);
    setLayout(layout);
}

NodeModel* NodeTableWidget::model() {
    return qobject_cast<NodeModel*>(tableView->model());
}

void NodeTableWidget::setModel(NodeModel* model) {
    tableView->setModel(model);
}
