#include <QApplication>
#include <QMainWindow>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QPushButton>
#include <QTabWidget>
#include <QTableView>

#include "gui/nodetablewidget.h"
#include "gui/common.h"

#include "fixture_p.h"

/*
 * Testing executable for comparing different approaches to
 * visualizing the data, using different tables
 *
 */

//
// Main
//
int main(int argc, char *argv[])
{
    QApplication *a = Plow::Gui::createQApp(argc, argv);

    QMainWindow w;
    w.resize(1200, 600);
    w.setWindowTitle("Node Manager");

    Plow::Gui::NodeModel model;
    Plow::Gui::DataFixture fixture(&model);
//    fixture.host_count = 25;

    QWidget* central = new QWidget;
    QVBoxLayout* layout = new QVBoxLayout(central);

    QPushButton* reloadLive = new QPushButton("Reload Live");
    reloadLive->setFixedWidth(130);
    QPushButton* reloadSim = new QPushButton("Reload Simulated");
    reloadSim->setFixedWidth(130);

    QHBoxLayout buttonLayout;
    buttonLayout.addStretch();
    buttonLayout.addWidget(reloadSim);
    buttonLayout.addWidget(reloadLive);

    // Update button will load generated data from a fixture
    QObject::connect(reloadSim, SIGNAL(clicked()), &fixture, SLOT(updateData()));
    QObject::connect(reloadLive, SIGNAL(clicked()), &model, SLOT(load()));

    QTabWidget* tab = new QTabWidget;

    // First table
    QTableView table1;
    table1.setModel(&model);
    tab->addTab(&table1, "Text Table");

    // Second Table
    Plow::Gui::NodeTableWidget table2;
    table2.setModel(&model);
    tab->addTab(&table2, "NodeWidget");

    layout->addLayout(&buttonLayout);
    layout->addWidget(tab);

    tab->setCurrentIndex(1);

    w.setCentralWidget(central);
    w.show();

    return a->exec();
}

