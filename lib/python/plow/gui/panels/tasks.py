
import os

import plow.client

from plow.client import TaskState 
from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.util import formatDuration
from plow.gui.event import EventManager
from plow.gui import constants 
from plow.gui.common.widgets import CheckableComboBox, TableWidget

IdRole = QtCore.Qt.UserRole
ObjectRole = QtCore.Qt.UserRole + 1


class TaskPanel(Panel):

    def __init__(self, name="Tasks", parent=None):
        Panel.__init__(self, name, "Tasks", parent)

        self.setAttr("refreshSeconds", 5)

        self.setWidget(TaskWidget(self.attrs, self))
        self.setWindowTitle(name)

        EventManager.bind("JOB_OF_INTEREST", self.__handleJobOfInterestEvent)

    def init(self):
        # TODO
        # sweep button (remove finished)
        # refresh button
        # seperator
        # kill button (multi-select)
        # comment button (multi-select)
        # 

        self.__state_filter = CheckableComboBox("Task States", constants.TASK_STATES, [], None, self)
        self.__layer_filter = CheckableComboBox("Layers", [], [], None, self)

        self.titleBarWidget().addWidget(self.__state_filter)    
        self.titleBarWidget().addWidget(self.__layer_filter)

    def openLoadDialog(self):
        print "Open search dialog"

    def openConfigDialog(self):
        pass

    def refresh(self):
        self.widget().refresh()

    def __handleJobOfInterestEvent(self, *args, **kwargs):
        self.widget().setJobId(args[0])


class TaskWidget(QtGui.QWidget):

    Width = [250, 90, 125, 100, 100, 65]
    Refresh = 1500

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__attrs = attrs
        
        self.__table = table = TableWidget(self)
        table.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)

        # connections
        table.customContextMenuRequested.connect(self.__showContextMenu)
        table.doubleClicked.connect(self.__rowDoubleClicked)

        self.__jobId = None
        self.__model = None

        self.layout().addWidget(table)

    def refresh(self):
        if self.__model:
            self.__model.refresh()

    def setJobId(self, jobid):
        new_model = False
        if not self.__model:
            self.__model = TaskModel(self)
            new_model = True

        self.__jobId = jobid
        self.__model.setJob(jobid)
        self.__table.setModel(self.__model)
        
        if new_model:
            table = self.__table
            for i, w in enumerate(self.Width):
                table.setColumnWidth(i, w)
    
    def __showContextMenu(self, pos):
        menu = QtGui.QMenu()
        menu.addAction(QtGui.QIcon(":/images/retry.png"), "Retry", self.retrySelected)
        menu.addAction(QtGui.QIcon(":/images/kill.png"), "Kill", self.killSelected)
        menu.addAction(QtGui.QIcon(":/images/eat.png"), "Eat", self.eatSelected)
        menu.exec_(self.mapToGlobal(pos))
        
    def __rowDoubleClicked(self, index):
        uid = index.data(IdRole)
        EventManager.emit("TASK_OF_INTEREST", uid, self.__jobId)

    def retrySelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.retry_tasks(taskIds=tasks)
            self.queueRefresh(self.Refresh, True)

    def killSelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.kill_tasks(taskIds=tasks)
            self.queueRefresh(self.Refresh, True)

    def eatSelected(self):
        tasks = self.getSelectedTaskIds()
        if tasks:
            plow.client.eat_tasks(taskIds=tasks)
            self.queueRefresh(self.Refresh, True)

    def getSelectedTaskIds(self):
        ids = []
        s_model = self.__table.selectionModel()
        for row in s_model.selectedRows():
            ids.append(row.data(IdRole))
        return ids

    def queueRefresh(self, ms, full=False):
        QtCore.QTimer.singleShot(ms, self.refresh)
        if full:
            EventManager.emit("GLOBAL_REFRESH")


class TaskModel(QtCore.QAbstractTableModel):

    HEADERS = ["Name", "State", "Node", "Resources", "Duration", "Retries", "Log"]

    HEADER_CALLBACKS = {
        0 : lambda t: t.name,
        1 : lambda t: constants.TASK_STATES[t.state],
        2 : lambda t: t.lastResource,
        3 : lambda t: "%s/%02dMB" % (t.stats.cores, t.stats.ram),
        4 : lambda t: formatDuration(t.stats.startTime, t.stats.stopTime),
        5 : lambda t: t.stats.retryNum,
        6 : lambda t: t.stats.lastLogLine,
    }

    def __init__(self, parent=None):
        QtCore.QAbstractTableModel.__init__(self, parent)
        self.__tasks = []
        self.__index = {}
        self.__jobId = None
        self.__lastUpdateTime = 0

        # A timer for refreshing seconds.
        self.__timer = QtCore.QTimer(self)
        self.__timer.timeout.connect(self.__durationRefreshTimer)

    def setJob(self, jobid):
        ## Clear out existing tasks.
        ## TODO make sure to emit right signals
        try:
            self.__timer.stop()
            self.beginResetModel()
            self.__tasks = []
            self.__index.clear()
            self.__jobId = jobid
            self.__tasks = plow.client.get_tasks(jobId=jobid)
            for i, task in enumerate(self.__tasks):
                self.__index[task.id] = i;
            self.__lastUpdateTime = plow.client.get_plow_time()
        finally:
            self.endResetModel()
            self.__timer.start(1000)

    def getJobId(self):
        return self.__jobId

    def refresh(self):
        if not self.__jobId:
            return

        t = plow.client.get_plow_time()
        tasks = plow.client.get_tasks(jobId=self.__jobId, lastUpdateTime=self.__lastUpdateTime)
        self.__lastUpdateTime = t

        count = len(self.HEADERS)-1
        for task in tasks:
            idx = self.__index[task.id]
            self.__tasks[idx] = task
            self.dataChanged.emit(self.index(idx,0), self.index(idx, count))

    def rowCount(self, parent=None):
        return len(self.__tasks)

    def columnCount(self, parent=None):
        return len(self.HEADERS)

    def data(self, index, role):
        row = index.row()
        col = index.column()
        task = self.__tasks[row]
        stats = task.stats 

        if role == QtCore.Qt.DisplayRole:
            cbk = self.HEADER_CALLBACKS.get(col)
            if cbk is not None:
                return cbk(task)
        
        elif role == QtCore.Qt.BackgroundRole and col ==1:
            return constants.COLOR_TASK_STATE[task.state]

        elif role == QtCore.Qt.TextAlignmentRole:
            if 0 < col < 6:
                return QtCore.Qt.AlignCenter

        elif role == QtCore.Qt.ToolTipRole and col == 3:
            tip = "Allocated Cores: %d\nCurrent CPU Perc:%d\n" \
                  "Max CPU Perc:%d\nAllocated RAM:%dMB\nCurrent RSS:%dMB\nMaxRSS:%dMB"
            return tip % (stats.cores, stats.usedCores, stats.highCores, 
                          stats.ram, stats.usedRam, stats.highRam)
        
        elif role == IdRole:
            return task.id
        
        elif role == ObjectRole:
            return task
        
        return

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.DisplayRole and orientation == QtCore.Qt.Horizontal:
            return self.HEADERS[section]

    def __durationRefreshTimer(self):
        RUNNING = plow.client.TaskState.RUNNING
        [self.dataChanged.emit(self.index(idx, 4),  self.index(idx, 4)) 
            for idx, t in enumerate(self.__tasks) if t.state == RUNNING]


class TaskWidgetConfigDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        pass




