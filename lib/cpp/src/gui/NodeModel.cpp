#include <QDebug>

#include "NodeModel.h"

namespace Plow {
namespace Gui {

//
// NodeModel
//

// TODO(justin): Temp Fixture Declaration
NodeList getHosts(int amount);
const int HOST_AMOUNT = 20000;

// NodeModel represents a list of NodeT instance
NodeModel::NodeModel(QObject *parent)
    : QAbstractTableModel(parent)
{}

NodeModel::~NodeModel() {
    nodes.clear();
}

// Callbacks for formatting the DisplayRole of
// each field in the NodeT struct.
Callbacks NodeModel::displayRoleCallbacks = Callbacks()
            << &DisplayRoleCallbacks::name
            << &DisplayRoleCallbacks::platform
            << &DisplayRoleCallbacks::cpuModel
            << &DisplayRoleCallbacks::clusterName
            << &DisplayRoleCallbacks::nodeState
            << &DisplayRoleCallbacks::lockState
            << &DisplayRoleCallbacks::totalCores
            << &DisplayRoleCallbacks::idleCores
            << &DisplayRoleCallbacks::totalRamMb
            << &DisplayRoleCallbacks::freeRamMb
            << &DisplayRoleCallbacks::totalSwapMb
            << &DisplayRoleCallbacks::freeSwapMb
            << &DisplayRoleCallbacks::bootTime;

QStringList NodeModel::headerLabels = QStringList()
        << "Host"
        << "Platform"
        << "Cpu Model"
        << "Cluster"
        << "State"
        << "Locked"
        << "Cores (Total)"
        << "Cores (Idle)"
        << "Ram (Total)"
        << "Ram (Free)"
        << "Swap (Total)"
        << "Swap (Free)"
        << "Uptime";


int NodeModel::rowCount(const QModelIndex &parent) const {
    Q_UNUSED(parent);
    return static_cast<int>(nodes.size());
}

int NodeModel::columnCount(const QModelIndex &parent) const {
    Q_UNUSED(parent);
    return headerLabels.count();
}

QVariant NodeModel::data(const QModelIndex &index, int role) const {
    QVariant ret;

    if (!index.isValid())
        return ret;

//    int row = index.row();
//    int col = index.column();

//    if (0 <= row && row < rowCount(QModelIndex())) {
    if (role == Qt::DisplayRole) {
        NodeT aNode = nodes.at(index.row());
        ret = displayRoleCallbacks[index.column()](aNode);
    }
//    }

    return ret;
}

QVariant NodeModel::headerData(int section,
                               Qt::Orientation orientation,
                               int role) const {
    if (role != Qt::DisplayRole)
        return QVariant();

    if (orientation == Qt::Vertical)
        return QVariant(section);

    return QVariant(headerLabels.at(section));
}

// Retrieve a new list of host data from the data source
// Resets the model's internal data structure.
void NodeModel::populate() {
    beginResetModel();
    nodes.clear();

    // TODO(justin): Eventually pull real hosts from Thrift rpc
    nodes = getHosts(HOST_AMOUNT);

    endResetModel();
}

const NodeT* NodeModel::nodeFromIndex(const QModelIndex &index) const {
    if (index.isValid())
        return &(nodes.at(index.row()));

    return NULL;
}

// NodeProxyModel
//
// Subclass of a QSortFilterProxyModel.
// Provides specialized sorting and filtering specifically
// for a NodeModel.
NodeProxyModel::NodeProxyModel(QObject *parent)
    : QSortFilterProxyModel(parent)
{}

bool NodeProxyModel::lessThan(const QModelIndex &left,
                              const QModelIndex &right) const {
    QString leftString = sourceModel()->data(left).toString();
    QString rightString = sourceModel()->data(right).toString();

    bool is_less;
    bool is_int;

    int leftInt = leftString.toInt(&is_int);
    if (is_int) {
        int rightInt = rightString.toInt();
        is_less = leftInt < rightInt;
    } else {
        is_less = leftString < rightString;
    }

    return is_less;
}

//
// TODO(justin): Data Fixture
//
#define randInt(low, high) (qrand() % ((high + 1) - low) + low)

// Creates random NodeT instance
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

    for (int row = 0; row < amount; ++row) {
        i_cpus      = p_cpus.at(randInt(0, p_cpus.count() - 1));
        i_ram       = t_ram.at(randInt(0, t_ram.count() - 1));
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
}  // Gui
}  // Plow
