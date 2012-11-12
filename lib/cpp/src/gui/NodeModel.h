#ifndef NODEMODEL_H
#define NODEMODEL_H

#include <vector>
#include <QStandardItemModel>
#include <QMap>
#include <QPair>

#include "plow_types.h"

namespace Plow { namespace Gui {


template <class Key, class T> class InitializableQMap : public QMap<Key,T>
{
public:
    inline InitializableQMap<Key,T> &operator<< (const QPair<Key,T> &t)
    { this->insert(t.first,t.second); return *this; }
};


//
// NodeModel
//
typedef std::vector<Plow::NodeT> NodeList;

class NodeModel : public QStandardItemModel
{
    Q_OBJECT

public:
    NodeModel(QObject *parent = 0);

    static const InitializableQMap<NodeState::type, QString> NODE_STATES;
    static const InitializableQMap<LockState::type, QString> LOCK_STATES;

signals:
    
public slots:
    void populate();


};


} // Gui
} // Plow

#endif // NODEMODEL_H


