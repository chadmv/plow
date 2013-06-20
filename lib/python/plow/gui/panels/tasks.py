
import os

import plow.client

from plow.client import TaskState 
from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.util import formatDuration
from plow.gui.event import EventManager
from plow.gui import constants 
from plow.gui.common.widgets import CheckableComboBox, TableWidget
from plow.gui.common import models
from plow.gui.dialogs.depends import DependencyWizard


class TaskPanel(Panel):

    def __init__(self, name="Tasks", parent=None):
        Panel.__init__(self, name, "Tasks", parent)

        self.setAttr("refreshSeconds", 5)

        self.setWidget(TaskWidget(self.attrs, self))
        self.setWindowTitle(name)

        self.__lastJobId = None

        EventManager.JobOfInterest.connect(self.__handleJobOfInterestEvent)

    def init(self):
        titleBar = self.titleBarWidget()

        self.__state_filter = CheckableComboBox("Task States", constants.TASK_STATES, parent=self)
        self.__layer_filter = CheckableComboBox("Layers", [], parent=self)

        titleBar.addWidget(self.__state_filter)    
        titleBar.addWidget(self.__layer_filter)

        self.__state_filter.optionSelected.connect(self.__stateFilterChanged)
        self.__layer_filter.optionSelected.connect(self.__layerFilterChanged)

    def openLoadDialog(self):
        print "Open search dialog"

    def openConfigDialog(self):
        pass

    def refresh(self):
        self.widget().refresh()

    def __handleJobOfInterestEvent(self, *args, **kwargs):
        jobId = args[0]
        taskWidget = self.widget()
        taskWidget.setJobId(args[0])

        if jobId != self.__lastJobId:
            self.__layer_filter.setOptions(taskWidget.layerNames)

        self.__lastJobId = jobId

    def __stateFilterChanged(self):
        sel = self.__state_filter.selectedOptions()
        self.widget().setStateFilters(sel)

    def __layerFilterChanged(self):
        sel = self.__layer_filter.selectedOptions()
        self.widget().setLayerFilters(sel)


class TaskWidget(QtGui.QWidget):

    WIDTH = [250, 90, 125, 100, 100, 65, 140]
    REFRESH = 1500

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__attrs = attrs

        self.__table = table = TableWidget(self)
        table.sortByColumn(0, QtCore.Qt.AscendingOrder)
        table.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)

        self.__jobId = None
        self.__layers = {}
        self.__model = None
        self.__proxy = proxy = TaskFilterProxyModel(self)
        proxy.setSortRole(TaskModel.SortRole)
        proxy.setDynamicSortFilter(True)
        proxy.sort(0, QtCore.Qt.AscendingOrder)
        table.setModel(proxy)

        self.layout().addWidget(table)

        # connections
        table.customContextMenuRequested.connect(self.__showContextMenu)
        table.doubleClicked.connect(self.__rowDoubleClicked)

    @property 
    def layers(self):
        return self.__layers.values()

    @property 
    def layerNames(self):
        return sorted(self.__layers.keys())

    def layerNameToId(self, name):
        layer = self.__layers.get(name)
        if layer:
            return layer.id 
        else:
            return ''

    def refresh(self):
        self.__updateLayers()

        if self.__model:
            self.__model.refresh()

    def setJobId(self, jobid):
        new_model = False
        if not self.__model:
            self.__model = TaskModel(self)
            self.__proxy.setSourceModel(self.__model)
            new_model = True

        if jobid != self.__jobId:
            self.__proxy.setFilters(layerIds=[])

        self.__jobId = jobid
        self.__updateLayers()

        self.__model.setJob(jobid)
        
        if new_model:
            table = self.__table
            for i, w in enumerate(self.WIDTH):
                table.setColumnWidth(i, w)
    
    def __showContextMenu(self, pos):
        menu = QtGui.QMenu()
        menu.addAction(QtGui.QIcon(":/images/retry.png"), "Retry", self.retrySelected)
        menu.addAction(QtGui.QIcon(":/images/kill.png"), "Kill", self.killSelected)
        menu.addAction(QtGui.QIcon(":/images/eat.png"), "Eat", self.eatSelected)

        total = self.__selectedCount()
        if 1 <= total <= 2:
            depend = menu.addAction(QtGui.QIcon(":/images/depend.png"), "Add Dependencies")
            depend.triggered.connect(self.__addDepends)

        menu.exec_(self.mapToGlobal(pos))
        
    def __rowDoubleClicked(self, index):
        uid = index.data(self.__model.IdRole)
        EventManager.TaskOfInterest.emit(uid, self.__jobId)

    def __updateLayers(self):
        if self.__jobId:
            self.__layers = dict((l.name, l) for l in plow.client.get_layers(self.__jobId))

    def __selectedCount(self):
        s_model = self.__table.selectionModel()
        return len(s_model.selectedRows())

    def setStateFilters(self, states):
        self.__proxy.setFilters(states=states)

    def setLayerFilters(self, layers):
        layer_set = set()
        allLayers = self.__layers

        for l in layers:

            obj = allLayers.get(l)
            if obj:
                layer_set.add(obj.id)

            elif isinstance(l, plow.client.Layer):
                layer_set.add(l.id)

            elif plow.client.is_uuid(l):
                layer_set.add(l)

        self.__proxy.setFilters(layerIds=layer_set)

    def retrySelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.retry_tasks(taskIds=tasks)
            self.queueRefresh(self.REFRESH, True)

    def killSelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.kill_tasks(taskIds=tasks)
            self.queueRefresh(self.REFRESH, True)

    def eatSelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.eat_tasks(taskIds=tasks)
            self.queueRefresh(self.REFRESH, True)

    def __addDepends(self):
        tasks = self.getSelectedTasks()
        if tasks:
            aTask = tasks[0]
            aJobSpec = plow.client.get_job_spec(aTask.jobId)

            wizard = DependencyWizard(self.parent(), project=aJobSpec.project)
            wizard.resize(wizard.width(), 600)
            wizard.setSourceObject(aTask)

            if len(tasks) > 1:
                wizard.setDestObject(tasks[1])

            wizard.show()

    def getSelectedTaskIds(self):
        ids = []
        s_model = self.__table.selectionModel()
        for row in s_model.selectedRows():
            ids.append(row.data(self.__model.IdRole))
        return ids

    def getSelectedTasks(self):
        tasks = []
        s_model = self.__table.selectionModel()
        for row in s_model.selectedRows():
            tasks.append(row.data(self.__model.ObjectRole))
        return tasks

    def queueRefresh(self, ms, full=False):
        QtCore.QTimer.singleShot(ms, self.refresh)
        if full:
            EventManager.GlobalRefresh.emit()


class TaskModel(models.PlowTableModel):

    HEADERS = ["Name", "State", "Node", "Resources", "Duration", "Retries", "Log"]

    DISPLAY_CALLBACKS = {
        0: lambda t: t.name,
        1: lambda t: constants.TASK_STATES[t.state],
        2: lambda t: t.stats.lastNode,
        3: lambda t: "%s/%02dMB" % (t.stats.cores, t.stats.ram),
        4: lambda t: formatDuration(t.stats.startTime, t.stats.stopTime),
        5: lambda t: t.stats.retryNum,
        6: lambda t: t.stats.lastLogLine,
    }

    SORT_CALLBACKS = {
        0: DISPLAY_CALLBACKS[0],
        1: DISPLAY_CALLBACKS[1],
        2: DISPLAY_CALLBACKS[2],
        3: lambda t: (t.stats.cores, t.stats.ram),
        4: lambda t: t.stats.stopTime - t.stats.startTime,
        5: DISPLAY_CALLBACKS[5],
        6: DISPLAY_CALLBACKS[6],
    }

    SortRole = models.PlowTableModel.DataRole

    def __init__(self, parent=None):
        super(TaskModel, self).__init__(parent)

        self.__jobId = None
        self.__lastUpdateTime = 0

        # Tasks are updated incrementally, so don't 
        # remove missing ones
        self.refreshShouldRemove = False

        # A timer for refreshing duration column.
        self.__timer = QtCore.QTimer(self)
        self.__timer.setInterval(1000)
        self.__timer.timeout.connect(self.__durationRefreshTimer)

    def fetchObjects(self):
        if not self.__jobId:
            return []

        opts = { "jobId": self.__jobId }
        if self.__lastUpdateTime:
            opts["lastUpdateTime"] = self.__lastUpdateTime 

        t = plow.client.get_plow_time()
        tasks = plow.client.get_tasks(**opts)
        self.__lastUpdateTime = t     

        return tasks   

    def getJobId(self):
        return self.__jobId

    def setJob(self, jobid):
        ## Clear out existing tasks.
        ## TODO make sure to emit right signals
        self.__timer.stop()

        self.__jobId = jobid
        self.__lastUpdateTime = 0

        try:
            tasks = self.fetchObjects()
            self.setItemList(tasks)

        finally:
            self.__timer.start()

    def refresh(self):
        if not self.__jobId:
            return

        super(TaskModel, self).refresh()

    def data(self, index, role):
        row = index.row()
        col = index.column()

        if role == QtCore.Qt.TextAlignmentRole:
            if 0 < col < 6:
                return QtCore.Qt.AlignCenter

        data = super(TaskModel, self).data(index, role)
        if data is not None:
            return data

        task = self._items[row]
        stats = task.stats 

        BG = QtCore.Qt.BackgroundRole
        FG = QtCore.Qt.ForegroundRole

        if col == 1 and (role == BG or role == FG):
            color = QtCore.Qt.black
            bgcolor = constants.COLOR_TASK_STATE[task.state]
            if bgcolor is constants.YELLOW:
                color = QtCore.Qt.white 
                
            if role == BG:
                return bgcolor
            else:
                return color

        elif role == TaskModel.SortRole:
            cbk = self.SORT_CALLBACKS.get(col)
            if cbk is not None:
                return cbk(task)

        elif role == QtCore.Qt.ToolTipRole and col == 3:
            tip = "Allocated Cores: %d\nCurrent CPU Perc:%d\n" \
                  "Max CPU Perc:%d\nAllocated RAM:%dMB\nCurrent RSS:%dMB\nMaxRSS:%dMB"
            return tip % (stats.cores, stats.usedCores, stats.highCores, 
                          stats.ram, stats.usedRam, stats.highRam)

    def __durationRefreshTimer(self):
        RUNNING = plow.client.TaskState.RUNNING
        for idx, t in enumerate(self._items):
            if t.state == RUNNING:
                self.dataChanged.emit(self.index(idx, 4),  self.index(idx, 4))


class TaskFilterProxyModel(models.AlnumSortProxyModel):

    def __init__(self, *args, **kwargs):
        super(TaskFilterProxyModel, self).__init__(*args, **kwargs)
        self.__states = set()
        self.__layers = set()

        self.__all_filters = (self.__states, self.__layers)
        self.__customFilterEnabled = False

    def setFilters(self, states=None, layerIds=None):
        if states is not None:
            self.__states.clear()
            for s in states:
                if isinstance(s, (str, unicode)):
                    s = constants.TASK_STATES.index(s)
                self.__states.add(s)

        if layerIds is not None:
            self.__layers.clear()
            self.__layers.update(layerIds)

        self.__customFilterEnabled = any(self.__all_filters)
        self.invalidateFilter()

    def filterAcceptsRow(self, row, parent):
        if not self.__customFilterEnabled:
            return super(TaskFilterProxyModel, self).filterAcceptsRow(row, parent)

        model = self.sourceModel()          
        idx = model.index(row, 0, parent)
        if not idx.isValid():
            return False

        task = model.data(idx, TaskModel.ObjectRole)
        if not task:
            return False

        states = self.__states
        if states and task.state not in states:
            return False

        layers = self.__layers
        if layers and task.layerId not in layers:
            return False

        return True


class TaskWidgetConfigDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        pass




