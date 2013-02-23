"""MainWindow"""
import os

from manifest import QtCore, QtGui
from panels.watch import RenderJobWatchPanel
from resources import icons

class DefaultConfig(object):

    # Default window size
    Size = QtCore.QSize(800, 600)

class MainWindow(QtGui.QMainWindow):
    """
    MainWindow class for all applications.  All that differentiates one
    tool from another is its dock widget layout.
    """
    def __init__(self, appname, layout=None):
        QtGui.QMainWindow.__init__(self, None)
        self.session = QtGui.QSessionManager(self)
        self.settings = QtCore.QSettings("plow", appname)
        self.workspace = WorkspaceManager(self)

        # If the active panel supports a keyword search then this
        # should be enabled, otherwise disabled.
        self.textSearch = QtGui.QLineEdit()
        self.textSearch.setMaximumWidth(200)

        spacer = QtGui.QWidget()
        spacer.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Expanding)

        # Only has workspace and search
        self.toolbar_top = QtGui.QToolBar(self)
        self.toolbar_top.addWidget(spacer)
        self.workspace.addWorkspaceSelectionMenu(self.toolbar_top)
        self.toolbar_top.addWidget(self.textSearch)
        self.addToolBar(QtCore.Qt.TopToolBarArea, self.toolbar_top)

        self.loadSettings()

        # Just for testing
        w = RenderJobWatchPanel("My Jobs", self)
        self.addDockWidget(QtCore.Qt.TopDockWidgetArea, w)

    def loadSettings(self):
        size = self.settings.value("main::size") or DefaultConfig.Size
        self.resize(size)

    def saveSettings(self):
        self.settings.setValue("main::size", self.size())


class WorkspaceManager(QtCore.QObject):
    
    DefaultOptions = ["Wranger", "Systems", "Artist", "Analysis"]

    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)

    def addWorkspaceSelectionMenu(self, obj):
        menu = QtGui.QMenu(obj)
        [menu.addAction(s) for s in self.DefaultOptions]
        
        # To-Do make active workspace the button label.
        action = QtGui.QToolButton(obj)
        action.setText("Workspace")
        action.setPopupMode(QtGui.QToolButton.InstantPopup)
        action.setMenu(menu)
        obj.addWidget(action)

def launch(argv, name, layout=None):
    # Initialize the default configuration files if none exist

    app = QtGui.QApplication(argv)
    app.setStyle("plastique")
    app.setStyleSheet(open(os.path.dirname(__file__) + "/resources/style.css").read())

    win = MainWindow(name, layout)
    app.lastWindowClosed.connect(win.saveSettings)
    win.show()
    app.exec_()
