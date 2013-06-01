"""The properites panel"""

import plow.client as pc

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.form import PlowForm
from plow.gui.util import formatCoreTime

class PropertiesPanel(Panel):

    def __init__(self, name="Properties", parent=None):
        Panel.__init__(self, name, "Properties", parent)

        self.setWidget(PropertiesWidget(self.attrs, self))
        self.setRefreshTime(30)
        self.setWindowTitle(name)

        EventManager.bind("JOB_OF_INTEREST", self.__handleJobOfInterestEvent)
        EventManager.bind("NODE_OF_INTEREST", self.__handleNodeOfInterestEvent)

        self.__object = None

    def init(self):

        self.titleBarWidget().addAction(
            QtGui.QIcon(":/images/retry.png"), "Refresh", self.refresh)

    def openConfigDialog(self):
        pass

    def refresh(self):
        if not self.__object:
            return
        if self.__object[0] == "job":
            self.displayJob(self.__object[1])
        elif self.__object[0] == "node":
            self.displayNode(self.__object[1])

    def displayJob(self, jobid):

        job = pc.get_job(jobid)
        self.__object = ("job",  jobid)

        widgets = [ {
                "title": "Job Status",
                "children": [
                    {
                        "title": "Name",
                        "widget": "text",
                        "value": job.name,
                        "readOnly": True
                    },
                    {
                        "title": "ID",
                        "widget": "text",
                        "value": job.id,
                        "readOnly": True
                    },
                    {
                        "title": "User",
                        "widget": "text",
                        "value": job.username,
                        "readOnly": True
                    },
                    {
                        "title": "UID",
                        "value": str(job.uid),
                        "widget": "text",
                        "readOnly": True
                    },
                    {
                        "title": "State",
                        "widget": "jobState",
                        "value": job.state
                    },
                    {
                        "title": "Progress",
                        "widget": "jobProgressBar",
                        "value": job.totals
                    },
                    {
                        "title": "Start Time",
                        "widget": "datetime",
                        "value": job.startTime
                    },
                    {
                        "title": "Stop Time",
                        "widget": "datetime",
                        "value": job.stopTime
                    },
                    {
                        "title": "Duration",
                        "widget": "duration",
                        "value": [job.startTime, job.stopTime]
                    },

                ]
            },
            {
                "title": "Core Totals",
                "children": [
                    {
                        "title": "Min Cores",
                        "value": job.minCores,
                    },
                    {
                        "title": "Max Cores",
                        "value": job.maxCores,
                    },
                    {
                        "title": "Run Cores",
                        "value": job.runCores,
                        "readOnly": True,
                    }
                ]
            },
            {
                "title": "Task Totals",
                "children": [
                    {
                        "title": "Running",
                        "value": job.totals.running,
                        "readOnly": True,
                    },
                    {
                        "title": "Succeeded",
                        "value": job.totals.succeeded,
                        "readOnly": True,
                    },
                    {
                        "title": "Depend",
                        "value": job.totals.depend,
                        "readOnly": True,
                    },
                    {
                        "title": "Dead",
                        "value": job.totals.dead,
                        "readOnly": True,       
                    },
                                    {
                        "title": "Waiting",
                        "value": job.totals.waiting,
                        "readOnly": True,
                    }
                ]
            },
            {
                "title": "Resource Stats",
                "children": [
                    {
                        "title": "High RAM (MB)",
                        "value": str(job.stats.highRam),
                        "maximumWidth": 125,
                        "readOnly": True,
                    },
                    {
                        "title": "High CPU",
                        "value": str(job.stats.highCores),
                        "readOnly": True,
                        "maximumWidth": 125,
                    },
                    {
                        "title": "High Core Time",
                        "value": formatCoreTime(job.stats.highCoreTime),
                        "readOnly": True,
                        "maximumWidth": 125,                  
                    },
                    {
                        "title": "Success Core Hours",
                        "value": [formatCoreTime(job.stats.totalSuccessCoreTime), "rgba(76, 115, 0, 192)"],
                        "widget": "pillWidget",                                 
                        "readOnly": True,
                        "maximumWidth": 125,    
                    },
                    {
                        "title": "Fail Core Hours",
                        "value":  [formatCoreTime(job.stats.totalFailCoreTime), "rgba(177, 24, 0, 192)"], 
                        "widget": "pillWidget",
                        "readOnly": True,
                        "maximumWidth": 125,
                    }
                ]
            }
        ]

        form = PlowForm(widgets)
        self.widget().setWidget(form)


    def displayNode(self, nodeid):

        node = pc.get_node(nodeid)
        self.__object = ("node",  nodeid)

        widgets = [ 
            {
                "title": "Node",
                "children": [
                    {
                        "title": "Name",
                        "widget": "text",
                        "value": node.name,
                        "readOnly": True
                    },
                    {
                        "title": "Cluster",
                        "widget": "text",
                        "value": node.clusterName,
                        "readOnly": True
                    },
                    {
                        "title": "IP Addr",
                        "widget": "text",
                        "value": node.ipaddr,
                        "readOnly": True
                    },
                    {
                        "title": "Tags",
                        "widget": "text",
                        "value": ",".join(node.tags),
                        "readOnly": True
                    },
                    {
                        "title": "Locked",
                        "widget": "lockToggle",
                        "value": node.locked,
                    }
                ]
            },
            {
                "title": "System",
                "children": [
                    {
                        "title": "CPU Model",
                        "widget": "text",
                        "value": node.system.cpuModel,
                        "readOnly": True
                    },
                    {
                        "title": "Platform",
                        "widget": "text",
                        "value": node.system.platform,
                        "readOnly": True
                    },
                ]
            }
        ]

        form = PlowForm(widgets)
        self.widget().setWidget(form)

    def __handleNodeOfInterestEvent(self, *args, **kwargs):
        self.displayNode(args[0])

    def __handleJobOfInterestEvent(self, *args, **kwargs):
        self.displayJob(args[0])

class PropertiesWidget(QtGui.QWidget):
    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.__attrs = attrs
        self.__scroll = QtGui.QScrollArea(self)
        self.__scroll.setWidgetResizable(True)
        self.__scroll.setFocusPolicy(QtCore.Qt.NoFocus)
        self.layout().addWidget(self.__scroll)


    def setWidget(self, widget):
        self.__scroll.setWidget(widget)

    def refresh(self):
        pass






