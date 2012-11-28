#include <QApplication>
#include <QMainWindow>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QPushButton>
#include <QList>
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
// TODO(justin): Data Fixture
//
namespace Plow {
namespace Gui {

#define randInt(low, high) (qrand() % ((high + 1) - low) + low)

DataFixture::DataFixture(NodeModel *aModel, QObject *parent)
    : QObject(parent)
{
    model = aModel;
    host_count = 10000;
}

// Creates random NodeT instance
NodeList DataFixture::getHosts(const int &amount) const {
    NodeList result(amount);

    QList<NodeState::type> nodeStates;
    nodeStates << NodeState::UP
               << NodeState::DOWN
               << NodeState::REPAIR
               << NodeState::REBOOT;

    QList<LockState::type> lockStates;
    lockStates << LockState::OPEN
               << LockState::LOCKED;

    QList<int> p_cpus;
    p_cpus << 1 << 2 << 4 << 8 << 16;

    QList<int> t_ram;
    t_ram << 4096 << 8192 << 16384;

    int i_cpus, i_ram, i_swap, i_uptime;

    NodeT aNode;

    for (int row = 0; row < amount; ++row) {
        i_cpus      = p_cpus.at(randInt(0, p_cpus.count() - 1));
        i_ram       = t_ram.at(randInt(0, t_ram.count() - 1));
        i_swap      = i_ram * .5;
        i_uptime    = randInt(10, 5 * 24 * 60 * 60);

        aNode = result[row];
        aNode.name = QString("Host%1").arg(row, 1, 10, QChar('0')).toStdString();
        aNode.clusterName = "General";
        aNode.state = nodeStates.at(randInt(0, nodeStates.count()-1));
        aNode.lockState = lockStates.at(randInt(0, lockStates.count()-1));
        aNode.totalCores = i_cpus;
        aNode.idleCores = i_cpus - randInt(0, i_cpus);
        aNode.bootTime = i_uptime;
        aNode.system.platform = "Linux";
        aNode.system.cpuModel = "Xeon 3.0Ghz";
        aNode.system.totalRamMb = i_ram;
        aNode.system.freeRamMb = i_ram - randInt(0, i_ram);
        aNode.system.totalSwapMb = i_swap;
        aNode.system.freeSwapMb = i_swap - randInt(0, i_swap);

        result[row] = aNode;
    }
    return result;
}

void DataFixture::updateData() {
    model->setNodeList(getHosts(host_count));
}

}  // Gui
}  // Plow

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

