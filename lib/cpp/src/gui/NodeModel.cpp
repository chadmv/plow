#include <QStandardItem>
#include <QMetaEnum>
#include <QDebug>
#include "NodeModel.h"

NodeModel::NodeModel(QObject *parent) :
    QStandardItemModel(parent)
{
    QStringList labels;
    labels << "Host" << "Platform" << "Cpu Model"
           << "Cluster" << "State"
           << "Cpus (Phys.)" << "Cpus (Log.)"
           << "Ram (Total)" << "Ram (Free)"
           << "Swap (Total)" << "Swap (Free)"
           << "Uptime" << "Rebooting";

    setHorizontalHeaderLabels(labels);
}

void NodeModel::populate() {
    while (rowCount()) {
        removeRow(0);
    }

    QStringList sList;
    QString aString;
    
    QList<QStandardItem*> aList;

    foreach (sList, getHosts(100)) {
        foreach (aString, sList) {
            aList << new QStandardItem(aString);
        }
        appendRow(aList);
        aList.clear();
    }

}

void NodeModel::refresh() {

}

QString NodeModel::stateToString(State aState) {
    int index = staticMetaObject.indexOfEnumerator("State");
    QMetaEnum metaEnum = staticMetaObject.enumerator(index);
    return QString(metaEnum.valueToKey(aState));
}



//
// Data Fixture - TEMP
//
#define randInt(low, high) (qrand() % ((high + 1) - low) + low)

/*
 * Create random "node" data
 */
QList<QStringList> getHosts(int amount) {
    QStringList states;
    states << "RUNNING" << "LOCKED" << "DOWN" << "IDLE";

    QStringList p_cpus;
    p_cpus << "1" << "2" << "4" << "8";

    QStringList t_ram;
    t_ram << "4096" << "8192" << "16384";

    QList<QStringList> result;
    QStringList aList;

    QString cpus, ram;
    int i_ram, i_swap, i_uptime;

    for (int row=0; row < amount; ++row) {

        cpus        = p_cpus.at(randInt(0,p_cpus.count()-1));
        ram         = t_ram.at(randInt(0,t_ram.count()-1));
        i_ram       = ram.toInt();
        i_swap      = i_ram * .5;
        i_uptime    = randInt(10, 5 * 24 * 60 * 60);

        aList << QString("Host%1").arg(row, 4, 10, QChar('0'))  // name
              << "Linux"  // platform
              << "Xeon XYZ CPU"  // cpu model
              << "General"  // cluster
              << states.at(randInt(0, states.count()-1))  // state
              << cpus  // physical cpus
              << QString::number(cpus.toInt() * randInt(1,2)) //logical cpus
              << ram  // total ram
              << QString::number(i_ram - randInt(0, i_ram)) // free ram
              << QString::number(i_swap)  // total swap
              << QString::number(i_swap - randInt(0, i_swap)) // free swap
              << QString::number(i_uptime) // uptime secs
              << "false"; // is rebooting
        result.append(aList);
        aList.clear();
    }
    return result;
}


