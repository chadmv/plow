
import os

import plow.client

from plow.client import TaskState 
from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.util import formatDuration
from plow.gui.event import EventManager
from plow.gui.constants import COLOR_TASK_STATE, TASK_STATES
from plow.gui.common.widgets import CheckableComboBox

class TaskPanel(Panel):

    def __init__(self, name="Tasks", parent=None):
        Panel.__init__(self, name, "Tasks", parent)

        self.setAttr("refreshSeconds", 10)

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

        self.__state_filter = CheckableComboBox("Task States", TASK_STATES, [], None, self)
        self.__layer_filter = CheckableComboBox("Layers", [], [], None, self)


        self.titleBarWidget().addAction(
            QtGui.QIcon(":/wrench.png"), "Configure", self.openConfigDialog)

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

    Header = ["Name", "State", "Node", "Resource", "Duration", "Log"]
    Width = [350]

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)
        self.__attrs = attrs
        
        self.__table = QtGui.QTableView(self)
        self.__table.setSelectionBehavior(QtGui.QTableView.SelectRows);
        self.__table.horizontalHeader().setStretchLastSection(True)
        self.__model = None

        self.layout().addWidget(self.__table)

    def setJobId(self, jobid):
        new_model = False
        if not self.__model:
            self.__model = TaskModel(self)
            new_model = True
        self.__model.setJob(jobid)
        self.__table.setModel(self.__model)
        if new_model:
            self.__table.setColumnWidth(0, self.Width[0])
        


class TaskModel(QtCore.QAbstractTableModel):
    def __init__(self, parent=None):
        QtCore.QAbstractTableModel.__init__(self, parent)
        self.__tasks = []
        self.__index = { }
        self.__lastUpdateTime = 0;

    def setJob(self, jobid):
        ## Clear out existing tasks.
        ## TODO make sure to emit right signals
        self.__tasks = []

        self.__tasks = plow.client.getTasks(jobId=jobid)
        for i, task in enumerate(self.__tasks):
            self.__index[task.id] = i;

    def refresh(self):
        t = plow.client.getPlowTime()
        tasks = plow.client.getTasks(jobId=self.__job.id, lastUpdateTime=self.__lastUpdateTime)
        self.__lastUpdateTime = t

        for task in tasks:
            idx = self.__index[task.id]
            self.__tasks[idx] = task
            self.dataChanged.emit(self.index(idx,0), self.index(idx, len(TaskView.Header)-1))

    def rowCount(self, parent=None):
        return len(self.__tasks)

    def columnCount(self, parent=None):
        return len(TaskWidget.Header)

    def data(self, index, role):
        row = index.row()
        col = index.column()
        task = self.__tasks[row]

        if role == QtCore.Qt.DisplayRole:
            if col == 0:
                return task.name
            elif col == 1:
                return TaskState._VALUES_TO_NAMES[task.state]
            elif col == 2:
                return task.lastNodeName
            elif col == 3:
                return "%s/%02dMB" % (task.cores, task.ramMb)
            elif col == 4:
                return formatDuration(task.startTime, task.stopTime)
            elif col == 5:
                return task.lastLogLine
        elif role == QtCore.Qt.BackgroundRole and col ==1:
            return COLOR_TASK_STATE[task.state]
        elif role == QtCore.Qt.ToolTipRole and col == 3:
            tip = "Allocated Cores: %d\nCurrent CPU Perc:%d\nMax CPU Perc:%d\nAllocated RAM:%dMB\nCurrent RSS:%dMB\nMaxRSS:%dMB"
            return tip % (task.cores, task.cpuPerc, task.maxCpuPerc, task.ramMb, task.rssMb, task.MaxRssMb)

        return

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.DisplayRole and orientation == QtCore.Qt.Horizontal:
            return TaskWidget.Header[section]


class TaskWidgetConfigDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        pass




