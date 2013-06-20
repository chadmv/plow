"""
The render job watch panel allows you to 
   1. setup filters to automatically load jobs. (defaults to loading your jobs)
   2. individually add jobs you want to watch.
"""
import os
import logging
from functools import partial

import plow.client
from plow.client import JobState, TaskState

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.common.widgets import CheckableListBox, BooleanCheckBox, SpinSliderWidget, ManagedListWidget
from plow.gui.common.job import JobProgressBar, JobSelectionDialog, JobStateWidget, JobContextMenu
from plow.gui import constants 
from plow.gui.util import formatMaxValue, formatDateTime, formatDuration
from plow.gui.event import EventManager


LOGGER = logging.getLogger(__name__)


JOBID_ROLE = QtCore.Qt.UserRole
JOB_ROLE = QtCore.Qt.UserRole + 1

class RenderJobWatchPanel(Panel):

    def __init__(self, name="Render Watch", parent=None):
        Panel.__init__(self, name, "Render Watch", parent)

        # Setup the default configuration
        self.setAttr("loadMine", True)
        self.setAttr("projects", [])
        self.setAttr("allProjects", True)
        self.setAttr("refreshSeconds", 5)
        self.setAttr("users", [])
        self.setAttr("loadedJobs", [])

        self.setWidget(RenderJobWatchWidget(self.attrs, self))
        self.setWindowTitle(name)

    def init(self):
        # TODO
        # comment button (multi-select)
        # 
        self.titleBarWidget().addAction(
            QtGui.QIcon(":/images/load.png"), "Load", self.openLoadDialog)

        self.titleBarWidget().addAction(
            QtGui.QIcon(":/images/sweep.png"), "Remove Finished Jobs", self.removeFinishedJobs)

    def openLoadDialog(self):
        dialog = JobSelectionDialog(parent=self)
        if dialog.exec_():
            widget = self.widget()
            for job in dialog.getSelectedJobs():
                widget.addJob(job) 

    def _openPanelSettingsDialog(self):
        self.__fixAttrs() 

        d = RenderJobWatchSettingsDialog(self.attrs)
        if d.exec_():
            attrs = d.getAttrs()
            attrs['allProjects'] = int(attrs['allProjects'])
            self.attrs.update(attrs)
            self.setRefreshTime(self.attrs["refreshSeconds"])

    def save(self, settings):
        widget = self.widget()
        ids = [j.id for j in widget.jobs()]
        self.setAttr("loadedJobs", ids)
        self.setAttr("allProjects", int(self.attrs["allProjects"]))
        super(RenderJobWatchPanel, self).save(settings)

    def restore(self, settings):
        super(RenderJobWatchPanel, self).restore(settings)
        self.__fixAttrs() 

        jobIds = self.getAttr("loadedJobs")
        if jobIds:
            QtCore.QTimer.singleShot(0, partial(self.__loadJobList, jobIds))

    def removeFinishedJobs(self):
         self.widget().removeFinishedJobs()

    def refresh(self):
        self.widget().refresh()

    def __fixAttrs(self):
        # Older PySide QSettings may serialize a single item
        # list to just a string
        for attr in ('users', 'projects', 'loadedJobs'):
            val = self.attrs[attr]
            if isinstance(val, (str, unicode)):
                self.attrs[attr] = [val]  

        self.attrs['allProjects'] = int(self.attrs['allProjects'])

    def __loadJobList(self, jobIds):
        widget = self.widget()
        jobs = plow.client.get_jobs(jobIds=jobIds)      
        for job in jobs:
            widget.addJob(job)


class RenderJobWatchWidget(QtGui.QWidget):

    HEADER = ["Job", "State", "Run", "Pend.", "Min", "Max", "Duration", "Progress"]
    WIDTH = [400, 75, 60, 60, 60, 60, 100, 125]

    COLOR_DEAD = constants.COLOR_TASK_STATE[TaskState.DEAD]
    COLOR_PAUSED = constants.BLUE

    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.attrs = attrs
        self.__jobs = {}

        self.__tree = tree = QtGui.QTreeWidget(self)
        tree.setHeaderLabels(self.HEADER)
        tree.setColumnCount(len(self.HEADER))
        tree.setUniformRowHeights(True)
        tree.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        tree.header().setStretchLastSection(True)
        tree.setSelectionMode(tree.ExtendedSelection)
        tree.setAlternatingRowColors(True)
        
        for i, v in enumerate(self.WIDTH):
            tree.setColumnWidth(i, v) 

        layout.addWidget(tree)

        # connections
        tree.itemDoubleClicked.connect(self.__itemDoubleClicked)
        tree.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)
        tree.customContextMenuRequested.connect(self.__showContextMenu)

    def refresh(self):
        self.__updateExistingJobs()
        self.__findNewJobs()

    def addJob(self, job):
        if self.__jobs.has_key(job.id):
            return False

        item = QtGui.QTreeWidgetItem([
            job.name,
            "",
            "%02d" % job.totals.running,
            "%02d" % (job.totals.waiting + job.totals.depend),
            "%02d" % job.minCores,
            formatMaxValue(job.maxCores),
            formatDuration(job.startTime, job.stopTime)
        ])

        center = QtCore.Qt.AlignCenter
        for i in xrange(2, item.columnCount()):
            item.setTextAlignment(i, center)

        self.__jobs[job.id] = item

        item.setToolTip(0, job.name)

        item.setToolTip(6, "Started: %s\nStopped:%s" % 
            (formatDateTime(job.startTime), formatDateTime(job.stopTime)))

        item.setData(0, JOBID_ROLE, job.id)
        item.setData(0, JOB_ROLE, job)

        self.__tree.addTopLevelItem(item)

        progress = JobProgressBar(job.totals, self.__tree)
        self.__tree.setItemWidget(item, len(self.HEADER)-1, progress);

        self.__setJobStateAndColor(item)

        return True

    def updateJob(self, job):
        item = self.__jobs[job.id]
        item.setData(0, JOB_ROLE, job)

        item.setText(2, "%02d" % job.totals.running)
        item.setText(3, "%02d" % job.totals.waiting)
        item.setText(4, "%02d" % job.minCores)
        item.setText(5, formatMaxValue(job.maxCores))
        item.setText(6, formatDuration(job.startTime, job.stopTime))

        item.setToolTip(6, "Started: %s\nStopped:%s" % 
            (formatDateTime(job.startTime), formatDateTime(job.stopTime)))

        self.__tree.itemWidget(item, len(self.HEADER)-1).setTotals(job.totals)
        self.__setJobStateAndColor(item)

    def removeFinishedJobs(self):
        finished = []
        for item in self.__jobs.itervalues():
            if item.data(0, JOB_ROLE).state == JobState.FINISHED:
                finished.append(item)

        for item in finished:
            self.removeJobItem(item)

    def removeJobItem(self, item):
        jobid = str(item.data(0, JOBID_ROLE))
        try:
            del self.__jobs[jobid]
        except Exception, e:
            LOGGER.error(e)
        idx = self.__tree.indexOfTopLevelItem(item)
        self.__tree.takeTopLevelItem(idx)

    def jobs(self):
        tree = self.__tree
        items = tree.findItems("", QtCore.Qt.MatchContains|QtCore.Qt.MatchRecursive)
        jobs = [item.data(0, JOB_ROLE) for item in items]      
        return jobs  

    def selectedJobs(self):
        tree = self.__tree
        jobs = [item.data(0, JOB_ROLE) for item in tree.selectedItems()]
        return jobs

    def __updateJobs(self, jobs):
        for job in jobs:
            if not self.__jobs.has_key(job.id):
                self.addJob(job)
            else:
                self.updateJob(job)

    def __updateExistingJobs(self):
        FINISHED = JobState.FINISHED
        now = QtCore.QDateTime.currentDateTimeUtc()
        toDateTime = QtCore.QDateTime.fromMSecsSinceEpoch

        req = {"matchingOnly": True}

        jobs = []
        for jobId, item in self.__jobs.iteritems():
            job = item.data(0, JOB_ROLE)

            # we will continue to pull updates on finished jobs for
            # 60 seconds longer
            if job.state == FINISHED:
                sec = toDateTime(job.stopTime).secsTo(now)
                if sec > 60:              
                    continue

            jobs.append(jobId)

        req["jobIds"] = jobs
        self.__updateJobs(plow.client.get_jobs(**req))

    def __findNewJobs(self):
        req = {}
        req["matchingOnly"] = True
        req["user"] = []
        req["states"] = [JobState.RUNNING]
        
        if self.attrs["loadMine"]:
            req["user"].append(os.environ["USER"])
        
        if self.attrs["users"]:
            req["user"].extend(self.attrs["users"])
       
        if self.attrs["projects"]:
            req["project"] = self.attrs["projects"]

        self.__updateJobs(plow.client.get_jobs(**req))

    def __showContextMenu(self, pos):
        tree = self.__tree
        item = tree.itemAt(pos)
        if not item:
            return 
        
        jobs = self.selectedJobs()

        menu = JobContextMenu(jobs, parent=tree)
        menu.popup(tree.mapToGlobal(pos))

    def __setJobStateAndColor(self, item):
        job = item.data(0, JOB_ROLE)
        totals = job.totals

        color = QtCore.Qt.black 
        text = constants.JOB_STATES[job.state]

        if job.paused:
            bgcolor = self.COLOR_PAUSED
            color = QtCore.Qt.white

        elif totals.dead:
            bgcolor = self.COLOR_DEAD
            color = QtCore.Qt.white

        else:
            bgcolor = constants.COLOR_JOB_STATE[job.state]

        item.setText(1, text)
        item.setBackground(1, bgcolor)
        item.setForeground(1, color)

    def __itemDoubleClicked(self, item, col):
        uid = item.data(0, JOBID_ROLE)
        EventManager.JobOfInterest.emit(uid)



class RenderJobWatchSettingsDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget.
    """
    def __init__(self, attrs, parent=None):
        QtGui.QDialog.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)

        self.sliderRefresh = SpinSliderWidget(1, 60, attrs["refreshSeconds"], self)
        self.sliderRefresh.slider.setTickInterval(5)
        self.sliderRefresh.slider.setTickPosition(QtGui.QSlider.TicksBelow)
        self.checkboxLoadMine = BooleanCheckBox(bool(attrs["loadMine"]))
        self.listUsers = ManagedListWidget(attrs["users"], "name", self)
        self.checkboxLoadErrors = QtGui.QCheckBox(self)

        projects = [project.code for project in plow.client.get_projects()]
        self.listProjects = CheckableListBox("Projects", 
                                                projects,
                                                attrs["projects"], 
                                                bool(attrs["allProjects"]), 
                                                self)

        group_box1 = QtGui.QGroupBox("Auto Load Jobs", self)

        form_layout1 = QtGui.QFormLayout(group_box1)
        form_layout1.addRow("Refresh", self.sliderRefresh)
        form_layout1.addRow("Load Mine:", self.checkboxLoadMine)
        form_layout1.addRow("Load User:", self.listUsers)
        form_layout1.addRow("Load With Errors:", self.checkboxLoadErrors)

        # move to project multi-select widget
        group_box2 = QtGui.QGroupBox("Filters", self)
        form_layout2 = QtGui.QFormLayout(group_box2)
        form_layout2.addRow("For Projects:", self.listProjects)

        buttons = QtGui.QDialogButtonBox(QtGui.QDialogButtonBox.Ok | QtGui.QDialogButtonBox.Cancel);
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)

        layout.addWidget(group_box1)
        layout.addWidget(group_box2)
        layout.addWidget(buttons)

    def getAttrs(self):
        return {
            "refreshSeconds": self.sliderRefresh.value(),
            "loadMine": self.checkboxLoadMine.isChecked(),
            "users": self.listUsers.getValues(),
            "projects": self.listProjects.getCheckedOptions(),
            "allProjects": self.listProjects.isAllSelected()
        }

