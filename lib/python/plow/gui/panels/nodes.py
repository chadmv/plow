import os
import logging
from datetime import datetime 

import plow.client

from plow.gui import constants
from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.common import models
from plow.gui.common.widgets import TableWidget, ResourceDelegate
from plow.gui.util import formatDuration

NODE_STATES = {}
for a in dir(plow.client.NodeState):
    if a.startswith('_'):
        continue
    val = getattr(plow.client.NodeState, a)
    NODE_STATES[val] = a


ObjectRole = QtCore.Qt.UserRole + 1

LOGGER = logging.getLogger(__name__)


class NodePanel(Panel):

    def __init__(self, name="Nodes", parent=None):
        Panel.__init__(self, name, "Nodes", parent)

        self.setAttr("refreshSeconds", 10)

        self.setWidget(NodeWidget(self.attrs, self))
        self.setWindowTitle(name)

    def init(self):
        pass

    def refresh(self):
        self.widget().refresh()


class NodeWidget(QtGui.QWidget):

    def __init__(self, attrs, parent=None):
        super(NodeWidget, self).__init__(parent)
        self.__attrs = attrs

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__model = model = NodeModel(self)
        self.__proxy = proxy = models.AlnumSortProxyModel(self)
        proxy.setSourceModel(model)

        self.__view = view = TableWidget(self)
        view.setModel(proxy)
        view.sortByColumn(0, QtCore.Qt.AscendingOrder)

        layout.addWidget(view)

        view.setColumnWidth(0, 150)
        view.setColumnWidth(model.HEADERS.index('Locked'), 60)
        view.setColumnWidth(model.HEADERS.index('Cores (Total)'), 90)
        view.setColumnWidth(model.HEADERS.index('Cores (Idle)'), 90)

        view.setColumnHidden(model.HEADERS.index('Ram (Total)'), True)
        view.setColumnHidden(model.HEADERS.index('Swap (Total)'), True)

        view.setItemDelegateForColumn(model.HEADERS.index('Ram (Free)'), 
                                      ResourceDelegate(parent=self))
        view.setItemDelegateForColumn(model.HEADERS.index('Swap (Free)'), 
                                      ResourceDelegate(warn=.75, critical=.25, parent=self))

        view.doubleClicked.connect(self.__itemDoubleClicked)

        
    def model(self):
        return self.proxyModel().sourceModel()

    def setModel(self, model):
        try:
            self.proxyModel().sourceModel().deleteLater()
        except:
            pass 
        self.proxyModel().setSourceModel(model)

    def refresh(self):
        self.__model.refresh()

    def __itemDoubleClicked(self, index):
        uid = index.data(ObjectRole).id
        EventManager.emit("NODE_OF_INTEREST", uid)


class NodeModel(QtCore.QAbstractTableModel):

    HEADERS = [
                "Name", "Cluster", 
                "State", "Locked", "Cores (Total)", "Cores (Idle)",
                "Ram (Total)", "Ram (Free)", "Swap (Total)",
                "Swap (Free)", "Ping", "Uptime"
               ]

    HEADER_CALLBACKS = {
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
        self.__items = []

    def hasChildren(self, parent):
        return False

    def reload(self):
        nodes = plow.client.get_nodes()
        self.setNodeList(nodes)

    def refresh(self):
        if not self.__items:
            self.reload()
            return 

        rows = self.__index
        colCount = self.columnCount()
        parent = QtCore.QModelIndex()

        nodes = plow.client.get_nodes()
        nodes_ids = set()
        to_add = set()

        # Update
        for node in nodes:
            nodes_ids.add(node.id)
            if node.id in self.__index:
                row = rows[node.id]
                self.__items[row] = node
                start = self.index(row, 0)
                end = self.index(row, colCount-1)
                self.dataChanged.emit(start, end)
                LOGGER.debug("updating %s %s", node.id, node.name)
            else:
                to_add.add(node)
        
        # Add new
        if to_add:
            size = len(to_add)
            start = len(self.__items)
            end = start + size - 1
            self.beginInsertRows(parent, start, end)
            self.__items.extend(to_add)
            self.endInsertRows()
            LOGGER.debug("adding %d new nodes", size)

        # Remove
        to_remove = set(self.__index.iterkeys()).difference(nodes_ids)
        for row, old_id in sorted(((rows[old_id], old_id) for old_id in to_remove), reverse=True):
            self.beginRemoveRows(parent, row, row)
            node = self.__items.pop(row)
            self.endRemoveRows()
            LOGGER.debug("removing %s %s", old_id, node.name)

        self.__index = dict((n.id, row) for row, n in enumerate(self.__items))

    def rowCount(self, parent):
        return len(self.__items)

    def columnCount(self, parent=None):
        return len(self.HEADERS)

    def data(self, index, role):
        if not index.isValid():
            return

        row = index.row()
        col = index.column()
        node = self.__items[row]

        if role == QtCore.Qt.DisplayRole or role == QtCore.Qt.ToolTipRole:
            return self.HEADER_CALLBACKS[col](node)

        elif role == QtCore.Qt.UserRole:
            if col == 7:
                return node.system.freeRamMb / float(node.system.totalRamMb)
            elif col == 9:
                return node.system.freeSwapMb / float(node.system.totalSwapMb)
            else:
                return self.HEADER_CALLBACKS[col](node) 

        elif role == QtCore.Qt.TextAlignmentRole:
            if col != 0:
                return QtCore.Qt.AlignCenter

        elif role == QtCore.Qt.BackgroundRole:
            if node.state == plow.client.NodeState.DOWN:
                return constants.RED 

            if node.locked:
                return constants.BLUE

            return None

        elif role == ObjectRole:
            return node

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.TextAlignmentRole:
            if section == 0:
                return QtCore.Qt.AlignLeft | QtCore.Qt.AlignVCenter 
            else:
                return QtCore.Qt.AlignCenter

        if role != QtCore.Qt.DisplayRole:
            return None 

        if orientation == QtCore.Qt.Vertical:
            return section 

        return self.HEADERS[section]

    def nodeFromIndex(self, idx):
        if not idx.isValid():
            return None 

        node = self.__items[idx.row()]
        return node

    def setNodeList(self, nodeList):
        self.beginResetModel()
        self.__items = nodeList
        self.__index = dict((n.id, row) for row, n in enumerate(nodeList))
        self.endResetModel()


