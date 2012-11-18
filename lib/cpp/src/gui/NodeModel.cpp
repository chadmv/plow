#include <QDebug>

#include "nodemodel.h"

namespace Plow {
namespace Gui {

//
// NodeModel represents a list of NodeT instance
//
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
void NodeModel::refresh() {
    NodeList aList;
    setNodeList(aList);
}

const NodeT* NodeModel::nodeFromIndex(const QModelIndex &index) const {
    if (index.isValid())
        return &(nodes.at(index.row()));

    return NULL;
}

// Resets the models internal data structure to the
// given NodeList
void NodeModel::setNodeList(const NodeList &aList) {
    beginResetModel();
    nodes.clear();
    nodes = aList;
    endResetModel();
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
}  // Gui
}  // Plow
