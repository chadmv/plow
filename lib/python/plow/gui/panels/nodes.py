import os
import logging
from datetime import datetime 
from functools import partial 

import plow.client

from plow.gui import constants
from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.common import models
from plow.gui.common.widgets import TableWidget, ResourceDelegate, CheckableComboBox
from plow.gui.util import formatDuration

NODE_STATES = {}
for a in dir(plow.client.NodeState):
    if a.startswith('_'):
        continue
    val = getattr(plow.client.NodeState, a)
    NODE_STATES[val] = a


LOGGER = logging.getLogger(__name__)

#########################
# NodePanel
#########################
class NodePanel(Panel):

    def __init__(self, name="Nodes", parent=None):
        Panel.__init__(self, name, "Nodes", parent)

        self.setAttr("refreshSeconds", 10)

        self.setWidget(NodeWidget(self.attrs, self))
        self.setWindowTitle(name)

    def init(self):
        titleBar = self.titleBarWidget() 
        titleBar.addAction(QtGui.QIcon(":/images/locked.png"), 
                                       "Lock Selected Nodes", 
                                       partial(self.__setNodesLocked, True))

        titleBar.addAction(QtGui.QIcon(":/images/unlocked.png"), 
                                       "Unlock Selected Nodes", 
                                       partial(self.__setNodesLocked, False))

        self.__cluster_filter = CheckableComboBox("Clusters", [], parent=self)
        titleBar.addWidget(self.__cluster_filter)
        self.__cluster_filter.optionSelected.connect(self.__clusterFilterChanged)

    def refresh(self):
        clusters = plow.client.get_clusters()
        names = sorted(c.name for c in clusters)

        nodeWidget = self.widget()
        nodeWidget.refresh()
        nodeWidget.setClusterList(clusters)

        filt = self.__cluster_filter
        sel = filt.selectedOptions()
        filt.setOptions(names, selected=sel)

    def __setNodesLocked(self, locked):
        try:
            for node in self.widget().getSelectedNodes():
                node.lock(locked)
        finally:
            self.refresh()

    def __clusterFilterChanged(self):
        sel = self.__cluster_filter.selectedOptions()
        self.widget().setClusterFilters(sel)


#########################
# NodeWidget
#########################
class NodeWidget(QtGui.QWidget):
    
    def __init__(self, attrs, parent=None):
        super(NodeWidget, self).__init__(parent)
        self.__attrs = attrs

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__refreshEnabled = True
        self.__clusters = {}

        self.__model = model = NodeModel(self)
        self.__proxy = proxy = NodeFilterProxyModel(self)
        proxy.setSortRole(model.DataRole)
        proxy.setSourceModel(model)

        self.__view = view = TableWidget(self)
        view.setModel(proxy)
        # view.sortByColumn(0, QtCore.Qt.AscendingOrder)

        layout.addWidget(view)

        view.setColumnWidth(0, 150)
        view.setColumnWidth(model.HEADERS.index('Locked'), 60)
        view.setColumnWidth(model.HEADERS.index('Cores (Total)'), 90)
        view.setColumnWidth(model.HEADERS.index('Cores (Idle)'), 90)

        view.setColumnHidden(model.HEADERS.index('Ram (Total)'), True)
        view.setColumnHidden(model.HEADERS.index('Swap (Total)'), True)

        delegate = ResourceDelegate(dataRole=model.DataRole, parent=self)
        view.setItemDelegateForColumn(model.HEADERS.index('Ram (Free)'), delegate)

        delegate = ResourceDelegate(warn=.75, critical=.25, dataRole=model.DataRole, parent=self)
        view.setItemDelegateForColumn(model.HEADERS.index('Swap (Free)'), delegate)

        view.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)

        view.customContextMenuRequested.connect(self.__showContextMenu)
        view.doubleClicked.connect(self.__itemDoubleClicked)

    def refresh(self):
        if self.__refreshEnabled:
            self.__view.setSortingEnabled(False)
            self.__model.refresh()
            self.__view.setSortingEnabled(True)

    def getSelectedNodes(self):
        rows = self.__view.selectionModel().selectedRows()
        return [index.data(self.__model.ObjectRole) for index in rows]

    def setClusterFilters(self, clusters):
        cluster_set = set()

        for c in clusters:

            if isinstance(c, plow.client.Cluster):
                cluster_set.add(c.name)

            elif isinstance(c, (str, unicode)):
                cluster_set.add(c)

        self.__proxy.setFilters(clusterNames=cluster_set)

    def setClusterList(self, clusters):
        self.__clusters = dict((c.name, c) for c in clusters)

    def assignClusterToSelected(self, cluster):
        nodes = self.getSelectedNodes()
        if not nodes:
            return

        size = len(nodes)
        progress = QtGui.QProgressDialog("Setting cluster to %s..." % cluster.name, 
                                         "Cancel", 0, size, self)

        progress.setMinimumDuration(3)
        completed = True
        did_set = False

        for i, node in enumerate(nodes):
            progress.setValue(i)

            if progress.wasCanceled():
                completed = False
                break

            if node.clusterName != cluster.name:
                node.set_cluster(cluster)
                did_set = True

            if i % 10 == 0:
                QtGui.qApp.processEvents()

        progress.setValue(size)
        QtGui.qApp.processEvents()

        if did_set:
            EventManager.GlobalRefresh.emit()

    def lockSelected(self, locked):
        nodes = self.getSelectedNodes()
        if nodes:
            did_lock = False
            for node in nodes:
                if node.locked != locked:
                    node.lock(locked)
                    did_lock = True

            if did_lock:
                EventManager.GlobalRefresh.emit()

    def __itemDoubleClicked(self, index):
        uid = index.data(self.__model.ObjectRole).id
        EventManager.NodeOfInterest.emit(uid)

    def __showContextMenu(self, pos):
        menu = QtGui.QMenu(self)

        cluster_menu = menu.addMenu("Set Cluster")
        for name, cluster in sorted(self.__clusters.iteritems()):
            action = cluster_menu.addAction(name)
            action.triggered.connect(partial(self.assignClusterToSelected, cluster))

        menu.addAction(QtGui.QIcon(":/images/locked.png"), 
                        "Lock Nodes", partial(self.lockSelected, True))
        menu.addAction(QtGui.QIcon(":/images/unlocked.png"), 
                        "Unlock Nodes", partial(self.lockSelected, False))

        self.__refreshEnabled = False
        menu.exec_(self.mapToGlobal(pos))
        self.__refreshEnabled = True

#########################
# NodeModel
#########################
class NodeModel(models.PlowTableModel):

    HEADERS = [
                "Name", "Cluster", 
                "State", "Locked", "Cores (Total)", "Cores (Idle)",
                "Ram (Total)", "Ram (Free)", "Swap (Total)",
                "Swap (Free)", "Ping", "Uptime"
               ]

    DISPLAY_CALLBACKS = {
        0 : lambda n: n.name,
        1 : lambda n: n.clusterName,
        2 : lambda n: NODE_STATES.get(n.state, ''),
        3 : lambda n: str(bool(n.locked)),
        4 : lambda n: n.totalCores,
        5 : lambda n: n.idleCores,
        6 : lambda n: n.system.totalRamMb,
        7 : lambda n: n.system.freeRamMb,
        8 : lambda n: n.system.totalSwapMb,
        9 : lambda n: n.system.freeSwapMb,
        10: lambda n: formatDuration(n.updatedTime), 
        11: lambda n: formatDuration(n.bootTime), 
    }

    def __init__(self, parent=None):
        super(NodeModel, self).__init__(parent)

    def fetchObjects(self):
        return plow.client.get_nodes()

    def reload(self):
        nodes = plow.client.get_nodes()
        self.setItemList(nodes)

    def refresh(self):
        if not self._items:
            self.reload()
            return 

        super(NodeModel, self).refresh()


    def data(self, index, role):
        row = index.row()
        col = index.column()
        node = self._items[row]

        if role == self.DataRole:
            if col == 7:
                return node.system.freeRamMb / float(node.system.totalRamMb)
            elif col == 9:
                return node.system.freeSwapMb / float(node.system.totalSwapMb)
            else:
                return self.DISPLAY_CALLBACKS[col](node) 

        elif role == QtCore.Qt.BackgroundRole:
            if node.state == plow.client.NodeState.DOWN:
                return constants.RED 

            if node.locked:
                return constants.BLUE

        return super(NodeModel, self).data(index, role)


#########################
# NodeFilterProxyModel
#########################
class NodeFilterProxyModel(models.AlnumSortProxyModel):

    def __init__(self, *args, **kwargs):
        super(NodeFilterProxyModel, self).__init__(*args, **kwargs)
        self.__clusters = set()

        self.__all_filters = (self.__clusters, )
        self.__customFilterEnabled = False

    def setFilters(self, clusterNames=None):
        if clusterNames is not None:
            self.__clusters.clear()
            self.__clusters.update(clusterNames)

        self.__customFilterEnabled = any(self.__all_filters)
        self.invalidateFilter()

    def filterAcceptsRow(self, row, parent):
        if not self.__customFilterEnabled:
            return super(NodeFilterProxyModel, self).filterAcceptsRow(row, parent)

        model = self.sourceModel()          
        idx = model.index(row, 0, parent)
        if not idx.isValid():
            return False

        node = model.data(idx, NodeModel.ObjectRole)
        if not node:
            return False

        clusters = self.__clusters
        if clusters and node.clusterName not in clusters:
            return False

        return True