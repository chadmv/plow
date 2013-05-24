import os
import logging
from datetime import datetime 
from itertools import chain, imap
from functools import partial

import plow.client
TaskState = plow.client.TaskState

import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.common import tree, models
from plow.gui.common.widgets import TreeWidget
from plow.gui.common.job import jobContextMenu
from plow.gui.util import formatDateTime, formatDuration

JOB_STATES = {}
TASK_STATES = {}

DATA_ROLE = models.DATA_ROLE
OBJECT_ROLE = DATA_ROLE + 1
TYPE_ROLE = DATA_ROLE + 2
ID_ROLE = DATA_ROLE + 3

INVALID_TYPE = 0
FOLDER_TYPE = 1
JOB_TYPE = 2


def _init():
    for a in dir(plow.client.JobState):
        if a.startswith('_'):
            continue
        val = getattr(plow.client.JobState, a)
        JOB_STATES[val] = a

_init()
del _init


LOGGER = logging.getLogger(__name__)


class JobWranglerPanel(Panel):

    def __init__(self, name="Job Wrangler", parent=None):
        Panel.__init__(self, name, "JobWrangler", parent)

        self.setAttr("refreshSeconds", 10)
        self.setAttr("projects", [])
        self.setAttr("allProjects", True)

        self.setWidget(JobWranglerWidget(self.attrs, self))
        self.setWindowTitle(name)

    def init(self):
        pass

    def refresh(self):
        self.widget().refresh()

    def save(self, settings):
        """
        Called when the application needs the planel to save its configuration.
        """
        for attr, val in self.attrs.iteritems():
            key = "panel::%s::%s" % (self.objectName(), attr)

            if attr == "projects":
                val = [p.id for p in val]

            settings.setValue(key, val)

    def restore(self, settings):
        """
        Called when the application needs the panel to restore its configuration.
        """
        if self.attrs.get("allProjects", False):
            self.attrs.pop("projects", None)

        for attr in self.attrs.keys():
            key = "panel::%s::%s" % (self.objectName(), attr)
            if settings.contains(key):
                val = settings.value(key)

                if attr == "allProjects" and val:
                    val = plow.client.get_projects()
                    self.attrs['projects'] = val
                    continue

                elif attr == "projects":
                    projects = []
                    for p_id in (val or []):
                        try:
                            projects.append(plow.client.get_project(p_id))
                        except:
                            pass   

                    val = projects              
                
                self.attrs[attr] = val
        
        if self.attrs.has_key("refreshSeconds"):
            self.setRefreshTime(self.attrs["refreshSeconds"])
            self.refresh()


class JobWranglerWidget(QtGui.QWidget):

    def __init__(self, attrs, parent=None):
        super(JobWranglerWidget, self).__init__(parent)
        self.__attrs = attrs

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        # DEBUG
        if not "projects" in attrs:
            attrs['projects'] = plow.client.get_projects()

        self.__model = model = JobModel(attrs, self)
        self.__proxy = proxy = models.AlnumSortProxyModel(self)
        proxy.setSourceModel(model)

        self.__view = view = TreeWidget(self)
        view.setModel(proxy)
        view.sortByColumn(4, QtCore.Qt.DescendingOrder)

        for i, width in enumerate(JobNode.HEADER_WIDTHS):
            view.setColumnWidth(i, width)

        view.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)
        view.customContextMenuRequested.connect(self.__showContextMenu)

        layout.addWidget(view)

        #
        # Connections
        #
        view.doubleClicked.connect(self.__itemDoubleClicked)
        view.activated.connect(self.__itemClicked)

        model.modelReset.connect(view.expandAll)

        self.__refreshTimer = timer = QtCore.QTimer(self)
        timer.setSingleShot(True)
        timer.setInterval(1500)
        timer.timeout.connect(self.refresh)


        
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

    def setProjects(self, projects):
        self.__attrs["projects"] = projects
        self.__model.setProjects(projects)

    def __itemDoubleClicked(self, index):
        if index.data(TYPE_ROLE) == JOB_TYPE:
            uid = index.data(ID_ROLE)
            EventManager.emit("JOB_OF_INTEREST", uid)

    def __itemClicked(self, index):
        if index.data(TYPE_ROLE) == FOLDER_TYPE:
            uid = index.data(ID_ROLE)
            EventManager.emit("FOLDER_OF_INTEREST", uid)

    def __showContextMenu(self, pos):
        tree = self.__view
        index = tree.indexAt(pos)
        if not index:
            return 

        typ = index.data(TYPE_ROLE)
        if typ != JOB_TYPE:
            return 

        job = index.data(OBJECT_ROLE)
        menu = jobContextMenu(job, partial(self.queueRefresh, True), tree)
        menu.popup(tree.mapToGlobal(pos))

    def queueRefresh(self, full=False):
        self.__refreshTimer.start()
        if full:
            EventManager.emit("GLOBAL_REFRESH")


class JobModel(tree.TreeModel):
    def __init__(self, attrs={}, parent=None):
        super(JobModel, self).__init__(parent)
        self.__attrs = attrs
        self.__folders = []
        self.__folder_index = {}

    def _getChildren(self):
        return [FolderNode(f, None, i) for i,f in enumerate(self.__folders)] 

    def data(self, index, role=QtCore.Qt.DisplayRole):
        if not index.isValid():
            return None 

        return index.internalPointer().data(index.column(), role)

    def index(self, row, column, parent=QtCore.QModelIndex()):
        return super(JobModel, self).index(row, column, parent)

    def columnCount(self, parent=QtCore.QModelIndex()):
        return len(JobNode.HEADERS)

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.DisplayRole and orientation == QtCore.Qt.Horizontal:
            return JobNode.HEADERS[section]

        elif role == QtCore.Qt.TextAlignmentRole:
            if section == 0:
                return QtCore.Qt.AlignLeft | QtCore.Qt.AlignVCenter 
            else:
                return QtCore.Qt.AlignCenter
                
        return None

    def reset(self):
        self.setFolderList([])

    def reload(self):
        projects = self.__attrs.get('projects', [])

        if not projects:
            self.reset()
            return 

        folders = chain.from_iterable(imap(plow.client.get_job_board, projects))
        self.setFolderList(list(folders))

    def refresh(self):
        projects = self.__attrs.get('projects', [])

        if not projects:
            self.reset()
            return 

        if not self.__folders:
            self.reload()
            return

        rows = self.__folder_index
        colCount = self.columnCount()
        parent = QtCore.QModelIndex()

        folder_ids = set()
        to_add = set()
        folderNodes = dict((f.ref.id, f) for f in self.subnodes)

        # pull the job board
        folders = chain.from_iterable(imap(plow.client.get_job_board, projects))

        # Update
        for folder in folders:

            folder_ids.add(folder.id)
            row = rows.get(folder.id)
            
            if row is None:
                to_add.add(folder)

            else:
                oldFolder = self.__folders[row]
                folderNode = folderNodes[folder.id]

                self.__updateJobs(folderNode, folder)
                folderNode.ref = folder

                self.__folders[row] = folder
                start = self.index(row, 0)
                end = self.index(row, colCount-1)
                self.dataChanged.emit(start, end)
                LOGGER.debug("updating %s %s", folder.id, folder.name)

        # Add new
        if to_add:
            size = len(to_add)
            start = len(self.__folders)
            end = start + size - 1
            self.beginInsertRows(parent, start, end)
            self.__folders.extend(to_add)
            self.invalidate()
            self.endInsertRows()
            LOGGER.debug("adding %d new folders", size)

        # Remove
        to_remove = ((rows[f_id], f_id) for f_id in set(rows).difference(folder_ids))
        for row, f_id in sorted(to_remove, reverse=True):
            self.beginRemoveRows(parent, row, row)
            folder = self.__folders.pop(row)
            self.subnodes.remove(folderNodes[f_id])
            self.endRemoveRows()
            LOGGER.debug("removing %s %s", f_id, folder.name)

        # re-index the rows
        self.__folder_index = dict((f.id, row) for row, f in enumerate(self.__folders))

    def itemByFolderId(self, f_id):
        row = self.__folder_index.get(f_id)
        if row is None:
            return None

        try:
            item = self.__folders[row]
        except IndexError:
            return None 

        return item

    def setFolderList(self, folders):
        self.beginResetModel()

        self.invalidate()
        self._dummyNodes = set()

        self.__folders = folders or []
        self.__folder_index = dict((f.id, row) for row, f in enumerate(folders))

        self.endResetModel()

    def __updateJobs(self, folderNode, newFolder):
        folderIndex = self.createIndex(folderNode.row, 0, folderNode)

        job_index = dict((j.id, j) for j in newFolder.jobs)
        oldJobNodes = folderNode.subnodes
        
        colCount = self.columnCount()
        updated = set()
        to_remove = set()

        # update existing
        for row, jobNode in enumerate(oldJobNodes):
            job = jobNode.ref 
            if job.id in job_index:
                jobNode.ref = job_index[job.id]
                updated.add(job.id)
                start = self.index(row, 0, folderIndex)
                end = self.index(row, colCount-1, folderIndex)
                self.dataChanged.emit(start, end)
                LOGGER.debug("updating job %s %s", job.id, job.name)
            else:
                to_remove.add((row, job))

        # add new
        new_jobs = updated.symmetric_difference(job_index)
        if new_jobs:
            size = len(new_jobs)
            start = len(oldJobNodes)
            end = start + size - 1
            self.beginInsertRows(folderIndex, start, end)
            oldJobNodes.extend((JobNode(job_index[j_id], folderNode, i) \
                                    for i, j_id in enumerate(new_jobs, start)))
            self.endInsertRows()
            LOGGER.debug("adding %d new nodes", size)            

        # remove
        for row, job in sorted(to_remove, reverse=True):
            self.beginRemoveRows(folderIndex, row, row)
            del oldJobNodes[row]
            self.endRemoveRows()
            LOGGER.debug("removing %s %s", job.id, job.name)            



#------------------------------------------
#
class PlowNode(tree.NodeContainer):

    TYPE = INVALID_TYPE
    HEADERS = []
    DISPLAY_CALLBACKS = []
    DATA_CALLBACKS = []

    def __init__(self, ref, parent, row):
        super(PlowNode, self).__init__()
        self._parent = parent
        self.row = row
        self.ref = ref

    def _getChildren(self):
        return []

    def parent(self):
        return self._parent

    def columnCount(self):
        return len(self.HEADERS)

    def data(self, column, role=QtCore.Qt.DisplayRole):
        if column >= self.columnCount():
            return None

        if role == OBJECT_ROLE:
            return self.ref

        if not self.ref:
            return None 

        if (role == QtCore.Qt.DisplayRole or role == QtCore.Qt.ToolTipRole): 
            if self.DISPLAY_CALLBACKS:
                return self.DISPLAY_CALLBACKS[column](self.ref)

        elif role == TYPE_ROLE:
            return self.TYPE

        elif role == ID_ROLE and self.ref:
            return self.ref.id

        elif role == DATA_ROLE:
            if self.DATA_CALLBACKS:
                return self.DATA_CALLBACKS[column](self.ref)

        return None


class FolderNode(PlowNode):

    TYPE = FOLDER_TYPE
    HEADERS = ["Name", "Running"]

    DISPLAY_CALLBACKS = [
        lambda f: f.name,
        lambda f: "{0} / {1}".format(f.totals.running, f.totals.total),
    ]

    DATA_CALLBACKS = [
        lambda f: f.name,
        lambda f: (f.totals.running / float(f.totals.total)) if f.totals.total else 0,
    ]

    def _getChildren(self):
        if not self.ref:
            return []
        return [JobNode(j, self, i) for i,j in enumerate(self.ref.jobs)]


class JobNode(PlowNode):

    TYPE = JOB_TYPE
    HEADERS = [
                "Name", "Running", "State", "Owner", 
                "Duration", "maxRam",
               ]

    HEADER_WIDTHS = (400,75,80,80,100,50)

    DISPLAY_CALLBACKS = [
        lambda j: j.name,
        lambda j: "{0} / {1}".format(j.totals.succeeded, j.totals.total),
        lambda j: "Paused" if j.paused else JOB_STATES.get(j.state, '').title(),
        lambda j: j.username,
        lambda j: formatDateTime(j.startTime),
        lambda j: j.stats.highRam,
    ]

    DATA_CALLBACKS = DISPLAY_CALLBACKS[:]
    DATA_CALLBACKS[2] = lambda j: (j.totals.succeeded / float(j.totals.total)) if j.totals.total else 0
    DATA_CALLBACKS[4] = lambda j: j.startTime

    def data(self, column, role=QtCore.Qt.DisplayRole):
        job = self.ref 

        DISP = QtCore.Qt.DisplayRole
        TOOL = QtCore.Qt.ToolTipRole
        BG = QtCore.Qt.BackgroundRole
        FG = QtCore.Qt.ForegroundRole

        # State
        if column == 2:
            if role == DISP or role == BG or role == FG:
                totals = job.totals
                color = QtCore.Qt.black 
                bgcolor = constants.COLOR_JOB_STATE[job.state]
                text = constants.JOB_STATES[job.state]

                if job.paused:
                    bgcolor = constants.BLUE
                    color = QtCore.Qt.white
                    text = "PAUSED"

                elif totals.dead:
                    bgcolor = constants.COLOR_TASK_STATE[TaskState.DEAD]
                    color = QtCore.Qt.white
                    text = "RUNNING" if totals.running else "DEAD"

                if role == BG:
                    return QtGui.QBrush(bgcolor)
               
                elif role == FG:
                    return QtGui.QBrush(color)
               
                else:
                    return text

        # Start time
        elif column == 4:
            if role == DISP:
                return formatDuration(job.startTime, job.stopTime)
            elif role == TOOL:
                return "Started: {0}\nStopped:{1}".format(
                            formatDateTime(job.startTime), 
                            formatDateTime(job.stopTime) )

        return super(JobNode, self).data(column, role)


if __name__ == "__main__":
    from plow.gui.util import loadTheme
    import sys 
    app = QtGui.QApplication(sys.argv)
    loadTheme()

    w = JobWranglerWidget({})
    w.resize(1024,800)
    w.show()
    w.refresh()

    app.exec_()
