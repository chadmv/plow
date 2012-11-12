#include <QStandardItem>
#include <QMetaEnum>
#include <QDebug>

#include <vector>

#include "plow_types.h"
#include "NodeModel.h"

namespace Plow { namespace Gui {

const InitializableQMap<NodeState::type, QString> NodeModel::NODE_STATES = \
        InitializableQMap<NodeState::type, QString>()
            << QPair<NodeState::type, QString>(NodeState::UP, QString("UP"))
            << QPair<NodeState::type, QString>(NodeState::DOWN, QString("DOWN"))
            << QPair<NodeState::type, QString>(NodeState::REPAIR, QString("REPAIR"))
            << QPair<NodeState::type, QString>(NodeState::REBOOT, QString("REBOOT"))
            ;

const InitializableQMap<LockState::type, QString> NodeModel::LOCK_STATES = \
        InitializableQMap<LockState::type, QString>()
            << QPair<LockState::type, QString>(LockState::OPEN, QString("OPEN"))
            << QPair<LockState::type, QString>(LockState::LOCKED, QString("LOCKED"))
            ;

NodeModel::NodeModel(QObject *parent) :
    QStandardItemModel(parent)
{
    QStringList labels;
    labels << "Host"
           << "Platform"        << "Cpu Model"
           << "Cluster"
           << "State"           << "Locked"
           << "Cores (Total)"   << "Cores (Idle)"
           << "Ram (Total)"     << "Ram (Free)"
           << "Swap (Total)"    << "Swap (Free)"
           << "Uptime";

    setHorizontalHeaderLabels(labels);
}

// Temp
NodeList getHosts(int amount);

void NodeModel::populate() {
    while (rowCount()) {
        removeRow(0);
    }

    QList<QStandardItem*> aList;

    NodeT aNode;
    NodeList nodeList = getHosts(100);

    for (NodeList::iterator it = nodeList.begin(); it < nodeList.end(); it++) {
        aNode = *it;

        aList << new QStandardItem(QString::fromStdString(aNode.name))
              << new QStandardItem(QString::fromStdString(aNode.system.platform))
              << new QStandardItem(QString::fromStdString(aNode.system.cpuModel))
              << new QStandardItem(QString::fromStdString(aNode.clusterName))
              << new QStandardItem(QString(NODE_STATES.value(aNode.state)))
              << new QStandardItem(QString(LOCK_STATES.value(aNode.lockState)))
              << new QStandardItem(QString::number(aNode.totalCores))
              << new QStandardItem(QString::number(aNode.idleCores))
              << new QStandardItem(QString::number(aNode.system.totalRamMb))
              << new QStandardItem(QString::number(aNode.system.freeRamMb))
              << new QStandardItem(QString::number(aNode.system.totalSwapMb))
              << new QStandardItem(QString::number(aNode.system.freeSwapMb))
              << new QStandardItem(QString::number(aNode.bootTime))
              ;

        appendRow(aList);
        aList.clear();
    }

}



//
// Data Fixture - TEMP
//
#define randInt(low, high) (qrand() % ((high + 1) - low) + low)

/*
 * Create random "node" data
 */
NodeList getHosts(int amount) {
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
    p_cpus << 1 << 2 << 4 << 8;

    QList<int> t_ram;
    t_ram << 4096 << 8192 << 16384;

    int i_cpus, i_ram, i_swap, i_uptime;

    NodeT aNode;

    for (int row=0; row < amount; ++row) {

        i_cpus      = p_cpus.at(randInt(0,p_cpus.count()-1));
        i_ram       = t_ram.at(randInt(0,t_ram.count()-1));
        i_swap      = i_ram * .5;
        i_uptime    = randInt(10, 5 * 24 * 60 * 60);

        aNode = result[row];
        aNode.name = QString("Host%1").arg(row, 4, 10, QChar('0')).toStdString();
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


} // Gui
} // Plow
