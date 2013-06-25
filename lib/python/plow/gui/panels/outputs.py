import logging

import plow.client
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager
from plow.gui.common import models
from plow.gui.common.widgets import TableWidget
                                    

LOGGER = logging.getLogger(__name__)

OBJECT_ROLE = models.PlowTableModel.ObjectRole
ID_ROLE = models.PlowTableModel.IdRole


class OutputPanel(Panel):

    def __init__(self, name="Outputs", parent=None):
        Panel.__init__(self, name, "Outputs", parent)

        self.setWidget(OutputWidget(self.attrs, self))
        self.setWindowTitle(name)

        EventManager.JobOfInterest.connect(self.__handleJobOfInterestEvent)
        EventManager.LayerOfInterest.connect(self.__handleLayerOfInterestEvent)

    def refresh(self):
        self.widget().refresh()

    def __handleJobOfInterestEvent(self, jobId, *args, **kwargs):
        self.widget().setJobId(jobId)

    def __handleLayerOfInterestEvent(self, layerId, *args, **kwargs):
        self.widget().setLayerId(layerId)


#########################
# OutputWidget
#########################
class OutputWidget(QtGui.QWidget):

    PATH_TYPE = 1000
    ATTR_TYPE = 1001

    def __init__(self, attrs, parent=None):
        super(OutputWidget, self).__init__(parent)
        
        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4,0,4,4)

        self.attrs = attrs

        self.__jobId = None 
        self.__layerId = None

        self.__tree = tree = QtGui.QTreeWidget(self)
        tree.setColumnCount(2)
        tree.setUniformRowHeights(True)
        tree.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        tree.setHeaderLabels(['Name', 'Value'])
        tree.setSelectionMode(tree.SingleSelection)
        tree.setAlternatingRowColors(True)
        tree.setColumnWidth(0, 300)

        tree.setDragEnabled(True)
        tree.startDrag = self.__startDrag

        layout.addWidget(tree)

        # connections
        tree.itemDoubleClicked.connect(self.__itemDoubleClicked)
        tree.setContextMenuPolicy(QtCore.Qt.CustomContextMenu)
        tree.customContextMenuRequested.connect(self.__showContextMenu)
        tree.itemClicked.connect(self.__copyValueToClipboard)

    def refresh(self):
        if self.__jobId:
            self.setJobId(self.__jobId)

        elif self.__layerId:
            self.setLayerId(self.__layerId)

    def setJobId(self, jobId):
        self.__jobId = jobId
        self.__layerId = None

        outputs = plow.client.get_job_outputs(jobId)
        self.setOutputs(outputs)

    def setLayerId(self, layerId):
        self.__jobId = None
        self.__layerId = layerId

        outputs = plow.client.get_layer_outputs(layerId)
        self.setOutputs(outputs)

    def setOutputs(self, outputs):
        tree = self.__tree 
        tree.clear()

        for output in outputs:

            path = output.path
            item = QtGui.QTreeWidgetItem([path], self.PATH_TYPE)

            item.setToolTip(0, path)
            item.setData(0, ID_ROLE, output.outputId)
            item.setData(0, OBJECT_ROLE, output)
            item.setIcon(0, QtGui.QIcon(":/images/resume.png"))
            item.setForeground(0, QtGui.QColor(255, 255, 255))
            item.setExpanded(True)

            for key, val in output.attrs.iteritems():
                child = QtGui.QTreeWidgetItem(item, [key, val], self.ATTR_TYPE)

            tree.addTopLevelItem(item)
            tree.setFirstItemColumnSpanned(item, True)

    def __startDrag(self, actions):
        texts = set()
        for item in self.__tree.selectedItems():

            if item.type() == self.PATH_TYPE:
                text = item.text(0)
            elif item.type() == self.ATTR_TYPE:
                text = item.text(1)

            texts.add(text)

        if not texts:
            return

        mimeData = QtCore.QMimeData()
        mimeData.setText(','.join(texts))

        drag = QtGui.QDrag(self)
        drag.setMimeData(mimeData)
        drag.start(QtCore.Qt.CopyAction)

    def __itemDoubleClicked(self, index):
        print "__itemDoubleClicked"

    def __showContextMenu(self, pos):
        print "__showContextMenu", pos

    def __copyValueToClipboard(self, item, col):
        clipboard = QtGui.QApplication.instance().clipboard()
        if item.childCount() == 0:
            value = item.text(1)
        else:
            value = item.text(0)
        clipboard.setText(value, clipboard.Selection)
        clipboard.setText(value, clipboard.Clipboard)




