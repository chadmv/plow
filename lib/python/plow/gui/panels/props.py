"""The properites panel"""

import plow.client as pc

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.form import PlowForm
from plow.gui.util import formatCoreTime, formatDuration

class PropertiesPanel(Panel):

    def __init__(self, name="Properties", parent=None):
        Panel.__init__(self, name, "Properties", parent)

        self.setWidget(PropertiesWidget(self.attrs, self))
        self.setRefreshTime(30)
        self.setWindowTitle(name)

        EventManager.JobOfInterest.connect(self.__handleJobOfInterestEvent)
        EventManager.NodeOfInterest.connect(self.__handleNodeOfInterestEvent)
        EventManager.LayerOfInterest.connect(self.__handleLayerOfInterestEvent)

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
        elif self.__object[0] == "layer":
            self.displayLayer(self.__object[1])

    def displayJob(self, jobid):

        job = pc.get_job(jobid)
        self.__object = ("job",  jobid)

        widgets = [ {
                "title": "Job Status",
                "children": [
                    { "title": "Name", "widget": "text", "value": job.name, "readOnly": True },
                    { "title": "ID", "widget": "text", "value": job.id, "readOnly": True }, 
                    { "title": "User", "widget": "text", "value": job.username, "readOnly": True },
                    { "title": "UID", "value": str(job.uid), "widget": "text", "readOnly": True },
                    { "title": "State", "widget": "jobState", "value": job.state },
                    { "title": "Progress", "widget": "jobProgressBar", "value": job.totals },
                    { "title": "Start Time", "widget": "datetime", "value": job.startTime },
                    { "title": "Stop Time", "widget": "datetime", "value": job.stopTime },
                    { "title": "Duration", "widget": "duration", "value": [job.startTime, job.stopTime] },
                ]
            },
            {
                "title": "Core Totals",
                "children": [
                    { "title": "Min Cores", "value": job.minCores },
                    { "title": "Max Cores", "value": job.maxCores },
                    { "title": "Run Cores", "value": job.runCores, "readOnly": True, }
                ]
            },
            {
                "title": "Task Totals",
                "children": [
                    { "title": "Running", "value": job.totals.running, "readOnly": True },
                    { "title": "Succeeded", "value": job.totals.succeeded, "readOnly": True },
                    { "title": "Depend", "value": job.totals.depend, "readOnly": True },
                    { "title": "Dead", "value": job.totals.dead, "readOnly": True },
                    { "title": "Waiting", "value": job.totals.waiting, "readOnly": True }
                ]
            },
            {
                "title": "Stats",
                "children": [
                    {
                        "title": "Memory",
                        "children": [
                            { "title": "High Memory (MB)", "value": job.stats.highRam, "readOnly": True },
                        ]
                    },
                    {
                        "title": "CPU Usage",
                        "children": [
                            { "title": "High CPU%", "value": job.stats.highCores, "readOnly": True },
                        ]
                    },
                    {
                        "title": "Core Hours",
                        "children": [
                            { "title": "Total", "value": formatCoreTime(job.stats.totalCoreTime), "readOnly": True },
                            { "title": "Succeeded", "value": [formatCoreTime(job.stats.totalSuccessCoreTime), 
                                "rgba(76, 115, 0, 192)"], "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "Failed", "value":  [formatCoreTime(job.stats.totalFailCoreTime), "rgba(177, 24, 0, 192)"],
                                "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "High Task", "value": formatCoreTime(job.stats.highCoreTime), "readOnly": True },
                        ]
                    },
                    {
                        "title": "Clock Hours",
                        "children": [
                            { "title": "High Task", "value": formatDuration(job.stats.highClockTime, -1), "readOnly": True },
                        ]
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
                    { "title": "Name", "widget": "text", "value": node.name, "readOnly": True },
                    { "title": "IP Addr", "widget": "text", "value": node.ipaddr, "readOnly": True },
                    { "title": "Cluster", "widget": "text", "value": node.clusterName, "readOnly": True },
                    { "title": "Tags", "widget": "text", "value": ",".join(node.tags), "readOnly": True },
                    { "title": "Created Time", "widget": "datetime", "value": node.createdTime },
                    { "title": "Updated Time", "widget": "datetime", "value": node.updatedTime },
                    { "title": "Boot Time", "widget": "datetime", "value": node.bootTime },
                    { "title": "Locked", "widget": "lockToggle", "value": node.locked }
                ]
            },
            {
                "title": "Dispatchable Resources",
                "children": [
                    { "title": "Total Cores", "value": node.totalCores },
                    { "title": "Idle Cores", "value": node.idleCores },
                    { "title": "Total Memory (MB)", "value": node.totalRamMb },
                    { "title": "Free Memory (MB)", "value": node.freeRamMb },
                ]
            },
            {
                "title": "System Info",
                "children": [
                    { "title": "CPU Model", "widget": "text", "value": node.system.cpuModel, "readOnly": True },
                    { "title": "Kernel", "widget": "text", "value": node.system.platform, "readOnly": True },
                    { "title": "Load", "value": node, "widget": "loadFactor", "readOnly": True },
                    { "title": "Physical CPUs", "value": node.system.physicalCores, "readOnly": True },
                    { "title": "Logical CPUs", "value": node.system.logicalCores, "readOnly": True },
                    { "title": "Total Memory (MB)", "value": node.system.totalRamMb, "readOnly": True },
                    { "title": "Free Memory (MB)", "value": node.system.freeRamMb, "readOnly": True },
                    { "title": "Total Swap (MB)", "value": node.system.totalSwapMb, "readOnly": True },
                    { "title": "Free Swap (MB)", "value": node.system.freeSwapMb, "readOnly": True }
                ]
            }
        ]

        form = PlowForm(widgets)
        self.widget().setWidget(form)

    def displayLayer(self, layerid):
        layer = pc.get_layer_by_id(layerid)
        self.__object = ("layer",  layerid)
        widgets = [ 
            {
                "title": "Layer",
                "children": [
                    { "title": "Name", "widget": "text", "value": layer.name, "readOnly": True },
                    { "title": "Service", "widget": "text", "value": layer.service, "readOnly": True },
                    { "title": "Range", "widget": "text", "value": layer.range, "readOnly": True },
                    { "title": "Chunk", "value": layer.chunk, "readOnly": True },
                ]
            },
            {
                "title": "Task Totals",
                "children": [
                    { "title": "Running", "value": layer.totals.running, "readOnly": True },
                    { "title": "Succeeded", "value": layer.totals.succeeded, "readOnly": True },
                    { "title": "Depend", "value": layer.totals.depend, "readOnly": True },
                    { "title": "Dead", "value": layer.totals.dead, "readOnly": True },  
                    { "title": "Waiting", "value": layer.totals.waiting, "readOnly": True }
                ]
            },
            {
                "title": "Stats",
                "children": [
                    {
                        "title": "Memory",
                        "children": [
                            { "title": "High Memory (MB)", "value": layer.stats.highRam, "readOnly": True },
                            { "title": "Avg Memory (MB)", "value": layer.stats.avgRam, "readOnly": True },
                            { "title": "Std Deviation", "value": layer.stats.stdDevRam, "readOnly": True },
                        ]
                    },
                    {
                        "title": "CPU Usage",
                        "children": [
                            { "title": "High CPU%", "value": layer.stats.highCores, "readOnly": True },
                            { "title": "Avg CPU%", "value": layer.stats.avgCores, "readOnly": True },
                            { "title": "Std Deviation", "value": layer.stats.stdDevCores, "readOnly": True },
                        ]
                    },
                    {
                        "title": "Core Hours",
                        "children": [
                            { "title": "Total", "value": formatCoreTime(layer.stats.totalCoreTime), "readOnly": True },
                            { "title": "Succeeded", "value": [formatCoreTime(layer.stats.totalSuccessCoreTime), "rgba(76, 115, 0, 192)"],
                                "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "Failed", "value":  [formatCoreTime(layer.stats.totalFailCoreTime), "rgba(177, 24, 0, 192)"],
                                "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "High Task", "value": formatCoreTime(layer.stats.highCoreTime), "readOnly": True },
                            { "title": "Avg Task", "value": formatCoreTime(layer.stats.avgCoreTime), "readOnly": True },
                            { "title": "Low Task", "value": formatCoreTime(abs(layer.stats.lowCoreTime)), "readOnly": True },
                        ]
                    },
                    {
                        "title": "Clock Hours",
                        "children": [
                            { "title": "Total", "value": formatDuration(layer.stats.totalClockTime, -1), "readOnly": True },
                            { "title": "Succeeded", "value": [formatDuration(layer.stats.totalSuccessClockTime, -1), "rgba(76, 115, 0, 192)"],
                                "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "Failed", "value":  [formatDuration(layer.stats.totalFailClockTime, -1), "rgba(177, 24, 0, 192)"],
                                "widget": "pillWidget", "readOnly": True, "maximumWidth": 125 },
                            { "title": "High Task", "value": formatDuration(layer.stats.highClockTime, -1), "readOnly": True },
                            { "title": "Avg Task", "value": formatDuration(layer.stats.avgClockTime, -1), "readOnly": True },
                            { "title": "Low Task", "value": formatDuration(abs(layer.stats.lowClockTime), -1), "readOnly": True },
                        ]
                    }
                ]
            },
            {
                "title": "Task Settings",
                "children": [
                    { "title": "Tags", "value": ",".join(layer.tags), "readOnly": True },
                    { "title": "Min Cores", "value": layer.minCores, "readOnly": True },
                    { "title": "Max Cores", "value": layer.maxCores, "readOnly": True },
                    { "title": "Min Memory (MB)", "value": layer.minRam, "readOnly": True },
                    { "title": "Max Memory (MB)", "value": layer.maxRam, "readOnly": True },
                    { "title": "Threadable", "value": layer.threadable, "readOnly": True },
                ]
            }
        ]
        
        form = PlowForm(widgets)
        self.widget().setWidget(form)

    def __handleLayerOfInterestEvent(self, *args, **kwargs):
        self.displayLayer(args[0])

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
        sp = self.__scroll.verticalScrollBar().value()
        self.__scroll.setWidget(widget)
        self.__scroll.verticalScrollBar().setValue(sp)

    def refresh(self):
        pass






