import os

import plow.client

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager

class LogsPanel(Panel):

    def __init__(self, name="Logs", parent=None):
        Panel.__init__(self, name, "Logs", parent)

        self.setWidget(TabbedLogVieweWidget(self.attrs, self))
        self.setWindowTitle(name)

        EventManager.bind("TASK_OF_INTEREST", self.__handleTaskOfInterestEvent)

    def init(self):
        pass

    def refresh(self):
        pass

    def _openPanelSettingsDialog(self):
        pass

    def __handleTaskOfInterestEvent(self, *args, **kwargs):
        task = plow.client.get_task(args[0])
        job = plow.client.get_job(args[1])

        self.widget().addTask(job, task)

class TabbedLogVieweWidget(QtGui.QWidget):
    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)

        self.__tasks = { }

        self.__txt_search = QtGui.QLineEdit(self)
        self.__tabs = QtGui.QTabWidget(self)
        self.__tabs.setTabsClosable(True)
        self.__tabs.tabCloseRequested.connect(self.closeTab)

        self.__btn_find = QtGui.QPushButton("Find", self)
        self.__btn_find_prev = QtGui.QPushButton("Find Prev", self)
        self.__btn_find_all = QtGui.QPushButton("Find All", self)

        low_layout = QtGui.QHBoxLayout()
        low_layout.addWidget(self.__txt_search)
        low_layout.addWidget(self.__btn_find)
        low_layout.addWidget(self.__btn_find_prev)
        low_layout.addWidget(self.__btn_find_all)

        layout = QtGui.QVBoxLayout()
        layout.addWidget(self.__tabs)
        layout.addLayout(low_layout)
        self.setLayout(layout)

    def addTask(self, job, task):
        index = self.__tasks.get(task.id, None)
        if index is not None:
            self.__tabs.setCurrentIndex(index)
        else:
            index = self.__tabs.addTab(LogViewerWidget(job, task, {}, self), task.name)
            self.__tabs.setTabToolTip(index, plow.client.get_task_log_path(task))
            self.__tabs.setCurrentIndex(index)
            self.__tasks[task.id] = index
            
    def closeTab(self, index):
        taskId = None
        for k, v in self.__tasks.iteritems():
            if v == index:
                taskId = k
                break
        if taskId:
            del self.__tasks[taskId]
        self.__tabs.removeTab(index)


class LogViewerWidget(QtGui.QWidget):
    def __init__(self, job, task, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.__chk_tail = QtGui.QCheckBox("Tail log", self);

        self.__toolbar = QtGui.QToolBar(self)
        self.__toolbar.addWidget(self.__chk_tail)
        self.__toolbar.addWidget(QtGui.QLabel(job.name, self))


        self.__view = QtGui.QPlainTextEdit(self)
        font = self.__view.font()
        font.setPointSize(font.pointSize()-2)
        font.setWeight(font.Light)
        self.__view.setFont(font)
        self.__view.setReadOnly(True)
        #self.__view.setFocusPolicy(QtCore.Qt.NoFocus)

        layout = QtGui.QVBoxLayout()
        layout.addWidget(self.__toolbar)
        layout.addWidget(self.__view)
        self.setLayout(layout)

        path = plow.client.get_task_log_path(task)

        self.__stream = QtCore.QTextStream()
        self.__log_file = QtCore.QFile(path)
        if not self.__log_file.open(QtCore.QIODevice.ReadOnly | QtCore.QIODevice.Text):
            print "Could not open log file"
            return
        self.__stream.setDevice(self.__log_file)
        self.__view.setPlainText(self.__stream.readAll())

 




