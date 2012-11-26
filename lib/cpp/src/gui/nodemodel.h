#ifndef PLOW_GUI_NODEMODEL_H_
#define PLOW_GUI_NODEMODEL_H_

#include <QAbstractTableModel>
#include <QSortFilterProxyModel>
#include <QMetaEnum>
#include <QStringList>

#include <vector>

#include "plow/plow.h"

class QVariant;

namespace Plow {
namespace Gui {

//
// NodeModel
//
typedef std::vector<Plow::NodeT> NodeList;
typedef QList< QVariant (*)( NodeT const& ) > Callbacks;

class NodeModel
        : public QAbstractTableModel
{
    Q_OBJECT
    Q_ENUMS(NODE_STATE LOCK_STATE)

 public:
    explicit NodeModel(QObject *parent = 0);
    ~NodeModel();

    enum NODE_STATE {
        UP      = NodeState::UP,
        DOWN    = NodeState::DOWN,
        REPAIR  = NodeState::REPAIR,
        REBOOT  = NodeState::REBOOT
    };

    enum LOCK_STATE {
        OPEN    = LockState::OPEN,
        LOCKED  = LockState::LOCKED
    };

    inline int rowCount(const QModelIndex &parent) const{
        Q_UNUSED(parent);
        return static_cast<int>(nodes.size());
    }

    inline int columnCount(const QModelIndex &parent) const {
        Q_UNUSED(parent);
        return headerLabels.count();
    }

    QVariant data(const QModelIndex &index, int role) const;

    QVariant headerData(int section,
                        Qt::Orientation orientation,
                        int role) const;

    const NodeT* nodeFromIndex(const QModelIndex&) const;

    void setNodeList(const NodeList &aList);

    inline int indexOfHeaderName(const QString &value) const
    { return headerLabels.indexOf(value); }

 public slots:
    void refresh();

 private:
    NodeList nodes;

    static Callbacks displayRoleCallbacks;
    static QStringList headerLabels;

    template<typename T>
    static QString enumToString(const char*, T);

    struct DisplayRoleCallbacks;
};

// NodeModel::DisplayRoleCallbacks
struct NodeModel::DisplayRoleCallbacks {
    inline static QVariant name(NodeT const& n)
    { return QVariant(QString::fromStdString(n.name)); }

    inline static QVariant platform(NodeT const& n)
    { return QVariant(QString::fromStdString(n.system.platform)); }

    inline static QVariant cpuModel(NodeT const& n)
    { return QVariant(QString::fromStdString(n.system.cpuModel)); }

    inline static QVariant clusterName(NodeT const& n)
    { return QVariant(QString::fromStdString(n.clusterName)); }

    inline static QVariant nodeState(NodeT const& n)
    { return QVariant(enumToString("NODE_STATE", n.state)); }

    inline static QVariant lockState(NodeT const& n)
    { return QVariant(enumToString("LOCK_STATE", n.lockState)); }

    inline static QVariant totalCores(NodeT const& n)
    { return QVariant(n.totalCores); }

    inline static QVariant idleCores(NodeT const& n)
    { return QVariant(n.idleCores); }

    inline static QVariant totalRamMb(NodeT const& n)
    { return QVariant(n.system.totalRamMb); }

    inline static QVariant freeRamMb(NodeT const& n)
    { return QVariant(n.system.freeRamMb); }

    inline static QVariant totalSwapMb(NodeT const& n)
    { return QVariant(n.system.totalSwapMb); }

    inline static QVariant freeSwapMb(NodeT const& n)
    { return QVariant(n.system.freeSwapMb); }

    inline static QVariant bootTime(NodeT const& n)
    { return QVariant(qint64(n.bootTime)); }
};

// NodeMode::enumToString
template<typename T>
inline QString NodeModel::enumToString(const char* enumName, T enumVal) {
    int index = staticMetaObject.indexOfEnumerator(enumName);
    QMetaEnum metaEnum = staticMetaObject.enumerator(index);
    return QString(metaEnum.valueToKey(enumVal));
}

//
// NodeProxyModel
//
class NodeProxyModel
        : public QSortFilterProxyModel
{
    Q_OBJECT

 public:
    explicit NodeProxyModel(QObject *parent = 0);

    bool lessThan(const QModelIndex &left,
                  const QModelIndex &right) const;
 protected:
    bool lessThanAlphaNumeric(const QString &left,
                              const QString &right) const;
};
}  // Gui
}  // Plow

#endif  // PLOW_GUI_NODEMODEL_H_
