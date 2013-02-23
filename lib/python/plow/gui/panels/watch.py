"""
The render job watch panel allows you to 
   1. setup filters to automatically load jobs. (defaults to loading your jobs)
   2. individually add jobs you want to watch.
"""

from plow.gui.manifest import QtCore, QtGui
from base import Panel

class RenderJobWatchPanel(Panel):

    def __init__(self, name="My Jobs", parent=None):
        Panel.__init__(self, name, parent)

        # Parent will always be the main window
        # So its possible to setup if the search bar
        # should be enbled here.
        self.setWidget(RenderJobWatchWidget(self))
        self.setWindowTitle(name)
        
    def init(self):
        # TODO
        # button for sweep (remove finished)
        # refresh button
        # 
        self.titleBarWidget().addAction(
            QtGui.QIcon(":/search.png"), "Search", self.openSearchDialog)

    def openSearchDialog(self):
        print "Open search dialog"


class RenderJobWatchWidget(QtGui.QWidget):

    def __init__(self, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)

        self.__tree = QtGui.QTreeWidget(self)
        self.__tree.setHeaderLabels(["Job", "Status"])

        self.layout().addWidget(self.__tree)


class AutoPopulateConfigurationDialog(QtGui.QDialog):
    """
    A dialog box that lets you configure how the render job widget

    """

    pass
