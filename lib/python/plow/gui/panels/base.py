
from plow.gui.manifest import QtCore, QtGui

class PanelManager(QtCore.QObject):
    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)

class Panel(QtGui.QDockWidget):
    def __init__(self, widget, parent=None):
        QtGui.QDockWidget.__init__(self, parent)

    def getToolbar(self):
        return None