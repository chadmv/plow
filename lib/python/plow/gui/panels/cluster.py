import os

import plow.client
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.util import formatPercentage
from plow.gui.event import EventManager
from plow.gui.common.widgets import CheckableComboBox, SimplePercentageBarDelegate, \
                                    ManagedListWidget, BooleanCheckBox, FormWidgetLabel, \
                                    TreeWidget

IdRole = QtCore.Qt.UserRole
ObjectRole = QtCore.Qt.UserRole + 1

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
            QtGui.QIcon(":/images/settings.png"), "Edit Selected Cluster Configuration", self.openClusterPropertiesDialog)
        self.titleBarWidget().addAction(
            QtGui.QIcon(":/images/locked.png"), "Lock Selected Clusters", self.__handleClusterLock)
        self.titleBarWidget().addAction(
            QtGui.QIcon(":/images/unlocked.png"), "Unlock Selected Clusters", self.__handleClusterUnlock)

    def openLoadDialog(self):
        print "Open search dialog"

    def openClusterPropertiesDialog(self):
        try:
            cluster = self.widget().getSelectedClusters()[0]
            dialog = ClusterPropertiesDialog(cluster)
            if dialog.exec_():
                dialog.save()
                self.refresh()
        except IndexError:
            pass

    def refresh(self):
        self.widget().refresh()

    def __handleClusterLock(self):
        try:
            for cluster in self.widget().getSelectedClusters():
                cluster.lock(True)
        finally:
            self.refresh()

    def __handleClusterUnlock(self):
        try:
            for cluster in self.widget().getSelectedClusters():
                cluster.lock(True)
        finally:
            self.refresh()

    def __handleJobOfInterestEvent(self, *args, **kwargs):
        self.widget().setJobId(args[0])


class ClusterWidget(QtGui.QWidget):

    Header = ["Name", "Usage", "Nodes", "Locked", "Repair", "Down", "Cores", "Tags"]
    Width = [250, 90, 70, 70, 70, 70, 70, 150]

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__attrs = attrs
        
        self.__tree = tree = TreeWidget(self)
        tree.setItemDelegateForColumn(1, SimplePercentageBarDelegate(self))

        self.__model = ClusterModel(self)
        tree.setModel(self.__model)
        self.__model.refresh()

        for i,v in enumerate(self.Width):
            tree.setColumnWidth(i, v) 

        self.layout().addWidget(self.__tree)

        # connections
        tree.doubleClicked.connect(self.__itemDoubleClicked)

    def getSelectedClusters(self):
        rows = self.__tree.selectionModel().selectedRows()
        return [index.data(ObjectRole) for index in rows]

    def refresh(self):
        self.__model.refresh()

    def __itemDoubleClicked(self, index):
        uid = index.data(IdRole)
        EventManager.emit("CLUSTER_OF_INTEREST", uid)


class ClusterModel(QtCore.QAbstractTableModel):

    HEADER_CALLBACKS = {
        0 : lambda c: c.name,
        1 : lambda c: [c.total.runCores, c.total.cores],
        2 : lambda c: c.total.nodes,
        3 : lambda c: c.total.lockedNodes,
        4 : lambda c: c.total.repairNodes,
        5 : lambda c: c.total.downNodes,
        6 : lambda c: c.total.cores,
        7 : lambda c: ",".join(c.tags),
    }

    def __init__(self, parent=None):
        QtCore.QAbstractTableModel.__init__(self, parent)
        self.__items = plow.client.get_clusters()
        self.__index = dict([(item.id, i) for i, item in enumerate(self.__items)])
        self.__lastUpdateTime = 0;

        self.__iconLocked = QtGui.QIcon(":/images/locked.png")

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
            cbk = self.HEADER_CALLBACKS.get(col)
            if cbk is not None:
                return cbk(cluster)
        
        elif role == QtCore.Qt.BackgroundColorRole:
            if cluster.isLocked:
                return constants.BLUE
            elif col == 3 and cluster.total.lockedNodes:
                return constants.BLUE
            elif col == 4 and cluster.total.repairNodes:
                return constants.ORANGE
            elif col == 5 and cluster.total.downNodes:
                return constants.RED

        elif role == QtCore.Qt.TextAlignmentRole:
            if col != 0:
                return QtCore.Qt.AlignCenter

        elif role == QtCore.Qt.DecorationRole and col == 0:
            if cluster.isLocked:
                return self.__iconLocked

        elif role == IdRole:
            return cluster.id
       
        elif role == ObjectRole:
            return cluster

        elif role == QtCore.Qt.ToolTipRole:
            if col == 1:
                return "\n".join([
                    "Total Cores: %d" % cluster.total.cores,
                    "Running Cores: %d" % cluster.total.runCores,
                    "Idle Cores: %d" % cluster.total.idleCores,
                    "Usage: %s" % formatPercentage(cluster.total.runCores, cluster.total.cores)
                ])
            if col == 6:
                return "\n".join([
                    "Total Cores: %d" % cluster.total.cores,
                    "Running Cores: %d" % cluster.total.runCores,
                    "Idle Cores: %d" % cluster.total.idleCores,
                    "Up Cores: %d" % cluster.total.upCores,
                    "Down Cores: %d" % cluster.total.downCores,
                    "Repair Cores: %d" % cluster.total.repairCores,
                    "Locked Cores: %d" % cluster.total.lockedCores
                ])
            elif col == 2:
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

        elif role == QtCore.Qt.TextAlignmentRole:
            if section == 0:
                return QtCore.Qt.AlignLeft | QtCore.Qt.AlignVCenter 
            else:
                return QtCore.Qt.AlignCenter


class ClusterWidgetConfigDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        pass

class ClusterPropertiesDialog(QtGui.QDialog):
    """
    Dialog box for editing the properties of a single cluster.
    """
    def __init__(self, cluster, parent=None):
        QtGui.QDialog.__init__(self, parent)
        self.__cluster = cluster

        self.txt_name = QtGui.QLineEdit(self.__cluster.name, self)
        self.list_tags = ManagedListWidget(cluster.tags, self)
        self.cb_locked = BooleanCheckBox(cluster.isLocked)
        self.cb_default = BooleanCheckBox(cluster.isDefault)

        buttons = QtGui.QDialogButtonBox(
            QtGui.QDialogButtonBox.Ok | 
            QtGui.QDialogButtonBox.Cancel);
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)

        layout = QtGui.QFormLayout()
        layout.setLabelAlignment(QtCore.Qt.AlignLeft)
        layout.addRow(FormWidgetLabel("Name", "cluster.name"), self.txt_name)
        layout.addRow(FormWidgetLabel("Tags", "cluster.tags"), self.list_tags)
        layout.addRow(FormWidgetLabel("Locked", "cluster.locked"), self.cb_locked)
        layout.addRow(FormWidgetLabel("Default", "cluster.default"), self.cb_default)
        layout.addRow(buttons)
        self.setLayout(layout)

    def save(self):
        try:
            c = self.__cluster
            c.set_name(str(self.txt_name.text()))
            c.set_tags(self.list_tags.getValues())
            c.lock(self.cb_locked.isChecked())
            if self.cb_default.isChecked():
                c.set_default()
        except Exception, e:
            title = "Error Saving Cluster"
            text = str(e)
            QtGui.QMessageBox.critical(self, title, text)
