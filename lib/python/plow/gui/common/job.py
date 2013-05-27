"""Commonly used Job widgets."""

from functools import partial 

import plow.client
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.form import FormWidget, FormWidgetFactory



class JobProgressFormWidget(FormWidget):
    def __init__(self, value, parent=None):
        FormWidget.__init__(self, parent)
        self.setWidget(JobProgressBar(value, parent))

FormWidgetFactory.register("jobProgressBar", JobProgressFormWidget)


class JobStateFormWidget(FormWidget):
    def __init__(self, value, parent=None):
        FormWidget.__init__(self, parent)
        self.setWidget(JobStateWidget(value, False, parent))
        self._widget.setMinimumWidth(100)

FormWidgetFactory.register("jobState", JobStateFormWidget)


class JobProgressBar(QtGui.QWidget):
    # Left, top, right, bottom
    __PEN = QtGui.QColor(33, 33, 33)

    Margins = [5, 2, 10, 4]

    def __init__(self, totals, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.setTotals(totals)
        self.setSizePolicy(QtGui.QSizePolicy.Expanding,
            QtGui.QSizePolicy.Preferred)

        ## Missing ability to detect size
    
    def setTotals(self, totals):
        self.__totals = totals
        self.__values =  [
            totals.waiting,
            totals.running,
            totals.dead, 
            totals.eaten,
            totals.depend,
            totals.succeeded
        ]
        self.update()

    def paintEvent(self, event):

        total_width = self.width() - self.Margins[2]
        total_height = self.height() - self.Margins[3]
        total_tasks = float(self.__totals.total)

        bar = []
        for i, v in enumerate(self.__values):
            if v == 0:
                continue
            bar.append((total_width * (v / total_tasks), constants.COLOR_TASK_STATE[i + 1]))

        painter = QtGui.QPainter()
        painter.begin(self)
        painter.setRenderHints(
            painter.HighQualityAntialiasing |
            painter.SmoothPixmapTransform |
            painter.Antialiasing)
        painter.setPen(self.__PEN)

        move = 0
        for width, color in bar:
            painter.setBrush(color)
            rect = QtCore.QRectF(
                self.Margins[0],
                self.Margins[1],
                total_width,
                total_height)
            if move:
                rect.setLeft(move)
            move+=width
            painter.drawRoundedRect(rect, 3, 3)
        painter.end()
        event.accept()


class JobSelectionDialog(QtGui.QDialog):
    def __init__(self, parent=None):
        QtGui.QDialog.__init__(self, parent)
        self.__jobs = plow.client.get_jobs(matchingOnly=True, states=[plow.JobState.RUNNING])

        self.__txt_filter = QtGui.QLineEdit(self)
        self.__txt_filter.textChanged.connect(self.__filterChanged)

        jobs = [job.name for job in self.__jobs]
        self.__model = QtGui.QStringListModel(jobs, self)

        self.__proxyModel = proxy = QtGui.QSortFilterProxyModel(self)
        proxy.setSourceModel(self.__model)

        self.__list_jobs = view = QtGui.QListView(self)
        view.setSelectionMode(self.__list_jobs.ExtendedSelection)
        view.setModel(proxy)

        proxy.sort(0)
        proxy.setDynamicSortFilter(True)

        self.__btns = QtGui.QDialogButtonBox(
            QtGui.QDialogButtonBox.Ok | 
            QtGui.QDialogButtonBox.Cancel)

        layout = QtGui.QVBoxLayout(self)
        layout.addWidget(self.__txt_filter)
        layout.addWidget(self.__list_jobs)
        layout.addWidget(self.__btns)

        # connections
        self.__list_jobs.doubleClicked.connect(self.accept)
        self.__btns.accepted.connect(self.accept)
        self.__btns.rejected.connect(self.reject)

    def __filterChanged(self, value):
        value = value.strip()
        if not value:
            self.__proxyModel.setFilterFixedString("")

        else:
            searchStr = '*'.join(value.split())
            self.__proxyModel.setFilterWildcard(searchStr)

    def getSelectedJobs(self):
        indexes = self.__list_jobs.selectionModel().selectedIndexes()
        jobNames = [self.__proxyModel.data(i) for i in indexes]

        if not jobNames:
            return jobNames

        return plow.client.get_jobs(matchingOnly=True, 
                                    name=jobNames, 
                                    states=[plow.JobState.RUNNING])


class JobStateWidget(QtGui.QWidget):
    """
    A widget for displaying the job state.
    """
    def __init__(self, state, hasErrors=False, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.__state = state
        self.__hasErrors = hasErrors
        self.setSizePolicy(QtGui.QSizePolicy.Minimum,
            QtGui.QSizePolicy.Preferred)

    def getState(self):
        return self.__state

    def hasErrors(self):
        return self.__hasErrors

    def setState(self, state, hasErrors):
        self.__state = state
        self.__hasErrors = hasErrors

    def paintEvent(self, event):

        total_width = self.width()
        total_height = self.height()

        painter = QtGui.QPainter()
        painter.begin(self)
        painter.setRenderHints(
            painter.HighQualityAntialiasing |
            painter.SmoothPixmapTransform |
            painter.Antialiasing)
        
        if self.__hasErrors:
            painter.setBrush(constants.RED)
        else:
            painter.setBrush(constants.COLOR_JOB_STATE[self.__state])
        
        painter.setPen(painter.brush().color().darker())

        rect = QtCore.QRect(0, 0, total_width, total_height)
        painter.drawRoundedRect(rect, 5, 5)
        painter.setPen(QtCore.Qt.black)
        painter.drawText(rect, QtCore.Qt.AlignCenter, constants.JOB_STATES[self.__state])
        painter.end()



def jobContextMenu(job, refreshCallback=None, parent=None):
    """
    Get a job context QMenu with common operations
    """
    menu = QtGui.QMenu(parent)

    pause = menu.addAction(QtGui.QIcon(":/images/pause.png"), "Un-Pause" if job.paused else "Pause")
    kill = menu.addAction(QtGui.QIcon(":/images/kill.png"), "Kill Job")
    kill_tasks = menu.addAction(QtGui.QIcon(":/images/kill.png"), "Kill Tasks")
    eat = menu.addAction(QtGui.QIcon(":/images/eat.png"), "Eat Dead Tasks")
    retry = menu.addAction(QtGui.QIcon(":/images/retry.png"), "Retry Dead Tasks")

    pause.triggered.connect(partial(job.pause, not job.paused))
    eat.triggered.connect(partial(job.eat_dead_tasks, refreshCallback))
    retry.triggered.connect(partial(job.retry_dead_tasks, refreshCallback))
    kill.triggered.connect(partial(job.kill, "plow-wrangler"))
    kill_tasks.triggered.connect(partial(job.kill_tasks, refreshCallback))

    return menu

