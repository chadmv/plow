"""The properites panel"""

import plow.client as pc

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.form import PlowForm

class PropertiesPanel(Panel):

    def __init__(self, name="Properties", parent=None):
        Panel.__init__(self, name, "Properties", parent)

        self.setWidget(PropertiesWidget(self.attrs, self))
        self.setWindowTitle(name)

        EventManager.bind("JOB_OF_INTEREST", self.__handleJobOfInterestEvent)

    def init(self):
        pass

    def openConfigDialog(self):
        pass

    def refresh(self):
        self.widget().refresh()

    def __handleJobOfInterestEvent(self, *args, **kwargs):

        job = pc.get_job(args[0])

        widgets = [ {
            "title": "Job",
            "children": [
                {
                    "title": "Name",
                    "widget": "text",
                    "value": job.name,
                    "readOnly": True
                },
                {
                    "title": "Progress",
                    "widget": "jobProgressBar",
                    "value": job.totals
                },
                {
                    "title": "Min Cores",
                    "value": job.minCores,
                    "readOnly": True,
                },
                {
                    "title": "Totals",
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
                }
            ]
        }]

        form = PlowForm(widgets)
        self.widget().setWidget(form)


class PropertiesWidget(QtGui.QWidget):
    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)
        self.__attrs = attrs
        self.__scroll = QtGui.QScrollArea(self)
        self.__scroll.setWidgetResizable(True)
        self.__scroll.setFocusPolicy(QtCore.Qt.NoFocus)
        self.layout().addWidget(self.__scroll)


    def setWidget(self, widget):
        self.__scroll.setWidget(widget)






