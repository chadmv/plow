import os
import logging
from datetime import datetime 

import plow.client
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.common import models


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

        self.__model = model = NodeModel(self)
        self.__proxy = proxy = models.AlnumSortProxyModel(self)
        proxy.setSourceModel(model)

        self.__view = view = QtGui.QTableView(self)
        view.verticalHeader().hide()
        view.sortByColumn(0, QtCore.Qt.AscendingOrder)
        view.setEditTriggers(view.NoEditTriggers)
        view.setSelectionBehavior(view.SelectRows)
        view.setSelectionMode(view.ExtendedSelection)
        view.setSortingEnabled(True)
        view.setModel(proxy)
        view.setAlternatingRowColors(False)
        view.setAutoFillBackground(False)
        view.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        view.horizontalHeader().setStretchLastSection(True)

        layout.addWidget(view)

        view.setColumnHidden(8, True)
        view.setColumnHidden(10, True)

        view.setItemDelegateForColumn(9, ResourceDelegate(self))
        view.setItemDelegateForColumn(11, ResourceDelegate(self))

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


class ResourceDelegate(QtGui.QItemDelegate):

    COLOR_CRITICAL = constants.RED
    COLOR_WARN = constants.YELLOW
    COLOR_OK = constants.GREEN

    def paint(self, painter, opts, index):
        currentData = index.data(QtCore.Qt.UserRole)
        try:
            ratio = float(currentData)
        except:
            super(ResourceDelegate, self).paint(painter, opts, index)
            return 

        text = "%0.2f%%" % (ratio * 100)
        opt = QtGui.QStyleOptionViewItemV4(opts)
        opt.displayAlignment = QtCore.Qt.AlignRight|QtCore.Qt.AlignVCenter

        grad = QtGui.QLinearGradient(opt.rect.topLeft(), opt.rect.topRight())
        darkEnd = QtCore.Qt.transparent
        end = darkEnd 

        if ratio == 1:
            darkEnd = self.COLOR_OK
            end = darkEnd

        elif ratio <= .05:
            darkEnd = self.COLOR_CRITICAL.darker(135)
            end = self.COLOR_CRITICAL

        elif ratio <= .15:
            darkEnd = self.COLOR_WARN.darker(135)
            end = self.COLOR_WARN

        grad.setColorAt(0.0, self.COLOR_OK.darker(135))
        grad.setColorAt(min(ratio, 1.0), self.COLOR_OK)
        grad.setColorAt(min(ratio + .01, 1.0), end)
        grad.setColorAt(1.0, darkEnd)

        self.drawBackground(painter, opt, index)
        painter.fillRect(opt.rect, QtGui.QBrush(grad))
        self.drawDisplay(painter, opt, opt.rect, text)



class NodeModel(QtCore.QAbstractTableModel):

    HEADERS = [
                "Name", "Platform", "CpuModel", "Cluster", 
                "State", "Locked", "Cores (Total)", "Cores (Idle)",
                "Ram (Total)", "Ram (Free)", "Swap (Total)",
                "Swap (Free)", "Uptime"
               ]

    HEADER_CALLBACKS = {
        0 : lambda n: n.name,
        1 : lambda n: n.system.platform,
        2 : lambda n: n.system.cpuModel,
        3 : lambda n: n.clusterName,
        4 : lambda n: NODE_STATES.get(n.state, ''),
        5 : lambda n: str(bool(n.locked)),
        6 : lambda n: n.totalCores,
        7 : lambda n: n.idleCores,
        8 : lambda n: n.system.totalRamMb,
        9 : lambda n: n.system.freeRamMb,
        10: lambda n: n.system.totalSwapMb,
        11: lambda n: n.system.freeSwapMb,
        12: lambda n: datetime.fromtimestamp(n.bootTime).strftime("%Y-%m-%d %H:%M"), 
    }

    ALIGN_CENTER = frozenset(xrange(3,13))

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
            if col == 9:
                return node.system.freeRamMb / float(node.system.totalRamMb)
            elif col == 11:
                return node.system.freeSwapMb / float(node.system.totalSwapMb)
            else:
                return self.HEADER_CALLBACKS[col](node) 

        elif role == QtCore.Qt.TextAlignmentRole:
            if col in self.ALIGN_CENTER:
                return QtCore.Qt.AlignCenter

        elif role == ObjectRole:
            return node


    def headerData(self, section, orientation, role):
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


