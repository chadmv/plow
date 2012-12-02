#include <QList>

#include "fixture_p.h"

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
        aNode.id = QString("node_id_%1").arg(row).toStdString();
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

