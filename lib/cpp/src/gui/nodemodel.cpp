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

QVariant NodeModel::data(const QModelIndex &index, int role) const {
    QVariant ret;

    if (!index.isValid())
        return ret;

    NodeT aNode = nodes.at(index.row());

    if (role == Qt::DisplayRole) {
        ret = displayRoleCallbacks[index.column()](aNode);

    } else if (role == Qt::UserRole) {
        int col = index.column();

        if (col == indexOfHeaderName("Ram (Free)"))
            return QVariant(static_cast<double>(aNode.system.freeRamMb)
                            / aNode.system.totalRamMb);
        if (col == indexOfHeaderName("Swap (Free)"))
            return QVariant(static_cast<double>(aNode.system.freeSwapMb)
                            / aNode.system.totalSwapMb);

        ret = displayRoleCallbacks[col](aNode);
    }

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

const NodeT *NodeModel::nodeFromIndex(const QModelIndex &index) const {
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
{
    setSortRole(Qt::UserRole);
}

// NodeProxyModel::lessThan
//
// Provides sort comparisons on the proper data types
bool NodeProxyModel::lessThan(const QModelIndex &left,
                              const QModelIndex &right) const
{
    QVariant leftData = left.data(sortRole());
    if (leftData.type() == QVariant::String) {
        QString leftStr = leftData.toString();
        QString rightStr = right.data(Qt::UserRole).toString();
        return lessThanAlphaNumeric(leftStr, rightStr);
    }
    return QSortFilterProxyModel::lessThan(left, right);
}

// NodeProxyModel::lessThanAlphaNumeric
//
// Takes two QStrings and splits them up into
// lists of alpha and numeric groups. Performs
// numeric based comparison when appropriate.
bool NodeProxyModel::lessThanAlphaNumeric(const QString &left,
                                          const QString &right) const
{
    static QRegExp alnums("(\\d+|\\D+)");

    if (left == right)
        return false;

    int pos, leftInt, rightInt;
    bool isInt;
    QString leftItem, rightItem;
    QStringList leftList, rightList;
    bool leftIsInt, rightIsInt;

    pos = 0;
    while ((pos = alnums.indexIn(left, pos)) != -1) {
        leftList << alnums.cap(1);
        pos += alnums.matchedLength();
    }

    pos = 0;
    while ((pos = alnums.indexIn(right, pos)) != -1) {
        rightList << alnums.cap(1);
        pos += alnums.matchedLength();
    }

    for (int i = 0; i < std::min(leftList.length(), rightList.length()); ++i) {
        leftItem = leftList.at(i);
        rightItem = rightList.at(i);

        // if left and right components are both int and not
        // equal, to a numeric comparison as the result
        leftInt = leftItem.toInt(&leftIsInt);
        rightInt = rightItem.toInt(&rightIsInt);
        if ((leftIsInt && rightIsInt) && (leftInt != rightInt))
            return leftInt < rightInt;

        // if left and right are string and not equal, do a
        // string based comparison as result
        if (leftItem != rightItem)
            return leftItem < rightItem;
    }
    // fallback on just comparing the entire strings
    return left < right;
}

}  // Gui
}  // Plow
