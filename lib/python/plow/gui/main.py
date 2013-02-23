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

        self.__panels = [ ]

        # If the active panel supports a keyword search then this
        # should be enabled, otherwise disabled.
        self.textSearch = QtGui.QLineEdit()
        self.textSearch.setMaximumWidth(200)

        spacer = QtGui.QWidget()
        spacer.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Expanding)

        # Only has workspace and search
        self.toolbar_top = QtGui.QToolBar(self)
        self.toolbar_top.setObjectName("Toolbar")
        self.toolbar_top.addWidget(spacer)
        self.workspace.addWorkspaceSelectionMenu(self.toolbar_top)
        self.toolbar_top.addWidget(self.textSearch)
        self.addToolBar(QtCore.Qt.TopToolBarArea, self.toolbar_top)

        menubar = QtGui.QMenuBar()

        # Setup menu bar
        menu_panel = menubar.addMenu("Panels")
        menu_panel.addAction("Job Watch")
        menu_panel.addAction("Job Board")
        menu_panel.addAction("Host Board")
        menu_panel.addAction("Tasks")
        menu_panel.addAction("Layers")

        menu_window = menubar.addMenu("Window")
        menu_window.addAction("New Window")
        menu_window.addSeparator()

        self.setMenuBar(menubar)
        self.restoreApplicationState()

        # Just for testing
        w = RenderJobWatchPanel("My Jobs", self)
        self.addPanel(w)

    def restoreApplicationState(self):
        geo = self.settings.value("main::geometry")
        if geo:
            self.restoreGeometry(geo)
        
        winstate = self.settings.value("main::windowState")
        if winstate:
            self.restoreState(winstate);

    def saveApplicationState(self):
        self.settings.setValue("main::geometry", self.saveGeometry());
        self.settings.setValue("main::windowState", self.saveState());

        for panel in self.__panels:
            panel.save(self.settings)

    def addPanel(self, panel):
        self.addDockWidget(QtCore.Qt.TopDockWidgetArea, panel)
        self.__panels.append(panel)

    def panelClosed(self, panel):
        pass


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
    app.lastWindowClosed.connect(win.saveApplicationState)
    win.show()
    app.exec_()
