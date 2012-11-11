#ifndef NODEMODEL_H
#define NODEMODEL_H

#include <QStandardItemModel>

class NodeModel : public QStandardItemModel
{
    Q_OBJECT
    Q_ENUMS(State)

public:
    NodeModel(QObject *parent = 0);
    enum State { Idle, Locked, Running, Down };

    static QString stateToString(State);

signals:
    
public slots:
    void populate();
    void refresh();

};

QList<QStringList> getHosts(int amount);

#endif // NODEMODEL_H


