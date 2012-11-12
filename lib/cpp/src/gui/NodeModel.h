#ifndef NODEMODEL_H
#define NODEMODEL_H

#include <vector>
#include <QStandardItemModel>
#include <QMetaEnum>

#include "plow_types.h"

namespace Plow { namespace Gui {

//
// NodeModel
//
typedef std::vector<Plow::NodeT> NodeList;

class NodeModel : public QStandardItemModel
{
    Q_OBJECT
    Q_ENUMS(NODE_STATE LOCK_STATE)

public:
    NodeModel(QObject *parent = 0);

    enum NODE_STATE {
    	UP=NodeState::UP, 
    	DOWN=NodeState::DOWN, 
    	REPAIR=NodeState::REPAIR, 
    	REBOOT=NodeState::REBOOT
    };

    enum LOCK_STATE {
    	OPEN=LockState::OPEN, 
    	LOCKED=LockState::LOCKED
    };

    template<typename T>
    QString enumToString(const char*, T) const;


public slots:
    void populate();

};

template<typename T>
inline QString NodeModel::enumToString(const char* enumName, T enumVal) const {
    int index = staticMetaObject.indexOfEnumerator(enumName);
    QMetaEnum metaEnum = staticMetaObject.enumerator(index);
    return QString(metaEnum.valueToKey(enumVal));
}

} // Gui
} // Plow

#endif // NODEMODEL_H


