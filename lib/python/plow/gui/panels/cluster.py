import os

import plow.client
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.util import formatPercentage
from plow.gui.event import EventManager
from plow.gui.common.widgets import CheckableComboBox, SimplePercentageBarDelegate

class ClusterPanel(Panel):

    def __init__(self, name="Clusters", parent=None):
        Panel.__init__(self, name, "Clusters", parent)

        self.setAttr("refreshSeconds", 10)

        self.setWidget(ClusterWidget(self.attrs, self))
        self.setWindowTitle(name)

    def init(self):
        # TODO
        # sweep button (remove finished)
        # refresh button
        # seperator
        # kill button (multi-select)
        # comment button (multi-select)
        # 
        self.titleBarWidget().addAction(
            QtGui.QIcon(":/wrench.png"), "Configure", self.openConfigDialog)


    def openLoadDialog(self):
        print "Open search dialog"

    def openConfigDialog(self):
        pass

    def refresh(self):
        self.widget().refresh()

    def __handleJobOfInterestEvent(self, *args, **kwargs):
        self.widget().setJobId(args[0])

class ClusterWidget(QtGui.QWidget):

    Header = ["Name", "Tags", "Usage", "Nodes", "Locked", "Repair", "Down", "Cores"]
    Width = [350]

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)
        self.__attrs = attrs
        
        self.__tree = QtGui.QTreeView(self)
        self.__tree.setSelectionBehavior(QtGui.QTableView.SelectRows);
        self.__tree.setItemDelegateForColumn(2, SimplePercentageBarDelegate(self))
        self.__tree.setAlternatingRowColors(True)
        self.__tree.setUniformRowHeights(True)
        self.__tree.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        self.__tree.setAutoFillBackground(True)

        self.__model = ClusterModel()
        self.__tree.setModel(self.__model)
        self.__model.refresh()

        [self.__tree.setColumnWidth(i, v) for i, v in enumerate(self.Width)]

        self.layout().addWidget(self.__tree)

class ClusterModel(QtCore.QAbstractTableModel):
    def __init__(self, parent=None):
        QtCore.QAbstractTableModel.__init__(self, parent)
        self.__items = plow.client.get_clusters()
        self.__index = dict([(item.id, i) for i, item in enumerate(self.__items)])
        self.__lastUpdateTime = 0;

        self.__iconLocked = QtGui.QIcon(":/locked.png")

    def hasChildren(self, parent):
        return False

    def refresh(self):
        updated = set()

        clusters = plow.client.get_clusters()
        for cluster in clusters:
            try:
                idx = self.__index[cluster.id]
                self.__items[idx] = cluster
                updated.add(cluster.id)
                self.dataChanged.emit(self.index(idx,0), self.index(idx, len(ClusterWidget.Header)-1))
            except IndexError:
                self.__items.append(cluster)
                self.__index[cluster.id] = len(self.__items) - 1

        # TODO: remove non updated clusters
        for removed in frozenset(self.__index.keys()).difference(updated):
            pass


    def rowCount(self, parent):
        if parent.isValid():
            return 0
        return len(self.__items)

    def columnCount(self, parent=None):
        if parent.isValid():
            return 0
        return len(ClusterWidget.Header)

    def data(self, index, role):
        row = index.row()
        col = index.column()
        cluster = self.__items[row]

        if role == QtCore.Qt.DisplayRole:
            if col == 0:
                return cluster.name
            elif col == 1:
                return ",".join(cluster.tags)
            elif col == 2:
                return [cluster.total.runCores, cluster.total.cores]
            elif col == 3:
                return cluster.total.nodes
            elif col == 4:
                return cluster.total.lockedNodes
            elif col == 5:
                return cluster.total.repairNodes
            elif col == 6:
                return cluster.total.downNodes
            elif col == 7:
                return cluster.total.cores
        elif role == QtCore.Qt.BackgroundColorRole:
            if cluster.isLocked:
                return constants.BLUE
            elif col == 4 and cluster.total.lockedNodes:
                return constants.BLUE
            elif col == 5 and cluster.total.repairNodes:
                return constants.ORANGE
            elif col == 6 and cluster.total.downNodes:
                return constants.RED

        elif role == QtCore.Qt.DecorationRole and col == 0:
            if cluster.isLocked:
                return self.__iconLocked
        elif role == QtCore.Qt.ToolTipRole:
            if col == 2:
                return "\n".join([
                    "Total Cores: %d" % cluster.total.cores,
                    "Running Cores: %d" % cluster.total.runCores,
                    "Idle Cores: %d" % cluster.total.idleCores,
                    "Usage: %s" % formatPercentage(cluster.total.runCores, cluster.total.cores)
                ])
            if col == 7:
                return "\n".join([
                    "Total Cores: %d" % cluster.total.cores,
                    "Running Cores: %d" % cluster.total.runCores,
                    "Idle Cores: %d" % cluster.total.idleCores,
                    "Up Cores: %d" % cluster.total.upCores,
                    "Down Cores: %d" % cluster.total.downCores,
                    "Repair Cores: %d" % cluster.total.repairCores,
                    "Locked Cores: %d" % cluster.total.lockedCores
                ])
            elif col == 3:
                return "\n".join([
                    "Total Nodes: %d" % cluster.total.nodes,
                    "Up Nodes: %d" % cluster.total.upNodes,
                    "Down Nodes: %d" % cluster.total.downNodes,
                    "Repair Nodes: %d" % cluster.total.repairNodes,
                    "Locked Nodes: %d" % cluster.total.lockedNodes,
                ])

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.DisplayRole and orientation == QtCore.Qt.Horizontal:
            return ClusterWidget.Header[section]


class ClusterWidgetConfigDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        pass




