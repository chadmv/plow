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
        self.panels = PanelManager(self)
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
        self.toolbar_top.setObjectName("Toolbar")
        self.toolbar_top.addWidget(spacer)
        self.workspace.addWorkspaceSelectionMenu(self.toolbar_top)
        self.toolbar_top.addWidget(self.textSearch)
        self.addToolBar(QtCore.Qt.TopToolBarArea, self.toolbar_top)

        menubar = QtGui.QMenuBar()

        # Setup menu bar
        self.panels.addPanelCreationMenu(menubar)

        menu_window = menubar.addMenu("Window")
        menu_window.addAction("New Window")
        menu_window.addSeparator()

        self.setMenuBar(menubar)
        self.restoreApplicationState()

    def restoreApplicationState(self):
        geo = self.settings.value("main::geometry")
        if geo:
            self.restoreGeometry(geo)
        
        winstate = self.settings.value("main::windowState")
        if winstate:
            self.restoreState(winstate)

        self.workspace.restoreState(self.settings)

    def saveApplicationState(self):
        self.settings.setValue("main::geometry", self.saveGeometry());
        self.settings.setValue("main::windowState", self.saveState());
        
        self.workspace.saveState(self.settings)

    def panelClosed(self, panel):
        pass

class PanelManager(QtCore.QObject):

    def __init__(self, parent=None):
        QtCore.QObject.__init__(self, parent)
        self.__panel_types = { }
        self.__panels = []

        self.registerPanelType("Job Watch", RenderJobWatchPanel)

    def registerPanelType(self, name, klass):
        if self.__panel_types.has_key(name):
            return
        self.__panel_types[name] = klass

    def addPanelCreationMenu(self, obj):
        menu = obj.addMenu("Panels")
        for ptype in self.__panel_types.keys():
            menu.addAction(ptype)
        menu.triggered.connect(self.__panelMenuTriggered)

    def createPanel(self, klass, name):
        p = klass(name, self.parent())
        self.__panels.append(p)
        self.parent().addDockWidget(QtCore.Qt.TopDockWidgetArea, p)
        return p

    def __panelMenuTriggered(self, action):
        name = str(action.text())
        self.createPanel(self.__panel_types[name], name)

class WorkspaceManager(QtCore.QObject):
    
    Defaults = ["Wrangler", "Systems", "Artist", "Production"]

    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)
        # Fix later
        self.__active = self.Defaults[0]
        self.__workspaces = list(self.Defaults)

    def addWorkspaceSelectionMenu(self, obj):
        menu = QtGui.QMenu(obj)
        [menu.addAction(s) for s in self.__workspaces]
        # TODO add custom workspaces
        menu.addSeparator()
        menu.addAction("Reset")
        menu.addAction("New Workspace")
        menu.triggered.connect(self.__menuItemTriggered)

        self.btn = QtGui.QToolButton(obj)
        self.btn.setText(self.__active)
        self.btn.setPopupMode(QtGui.QToolButton.InstantPopup)
        self.btn.setMenu(menu)
        obj.addWidget(self.btn)

    def saveState(self, settings):
        settings.setValue("main::workspace", self.__active)

    def restoreState(self, settings):
        # TODO: switch the workspace to whatever was active at shutdown
        ws = settings.value("main::workspace")
        if ws:
            self.setWorkspace(ws)

    def setWorkspace(self, name):
        self.btn.setText(name)
        self.__active = name
        # TODO
        # re-layout all the dock widget

    def __menuItemTriggered(self, action):
        name = action.text()
        if name in self.__workspaces:
            self.setWorkspace(name)

def launch(argv, name, layout=None):
    # Initialize the default configuration files if none exist

    app = QtGui.QApplication(argv)
    app.setStyle("plastique")
    app.setStyleSheet(open(os.path.dirname(__file__) + "/resources/style.css").read())

    win = MainWindow(name, layout)
    app.lastWindowClosed.connect(win.saveApplicationState)
    win.show()
    app.exec_()
