"""MainWindow"""
import os

from manifest import QtCore, QtGui
from panels import *
from resources import icons
from event import EventManager

class DefaultConfig(object):

    # Default window size
    Size = QtCore.QSize(800, 600)

class MainWindow(QtGui.QMainWindow):
    """
    MainWindow class for all applications.  All that differentiates one
    tool from another is its dock widget layout.
    """
    def __init__(self, appname, workspace):
        QtGui.QMainWindow.__init__(self, None)
        self.__default_workspace = workspace
        self.setDockOptions(self.AnimatedDocks | 
                            self.AllowNestedDocks |
                            self. AllowTabbedDocks |
                            self.VerticalTabs)

        self.session = QtGui.QSessionManager(self)
        self.settings = QtCore.QSettings("plow", appname)
        print self.settings.fileName()
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
        self.toolbar_top.toggleViewAction().setDisabled(True)

        self.addToolBar(QtCore.Qt.TopToolBarArea, self.toolbar_top)

        # Setup menu bar
        menubar = QtGui.QMenuBar()
        self.workspace.addPanelCreationMenu(menubar)

        menu_window = menubar.addMenu("Window")
        menu_window.addAction("New Window")
        menu_window.addSeparator()

        self.setMenuBar(menubar)
        self.restoreApplicationState()

    def contextMenuEvent(self, e):
        # Stops the context menu on the dock
        e.ignore()

    def restoreApplicationState(self):
        # The geometry of the window and current workspace
        # are saved in the main configuration file.
        geo = self.settings.value("main::geometry")
        if geo:
            self.restoreGeometry(geo)
        
        ws = self.settings.value("main::workspace")
        if not ws:
            ws = self.__default_workspace

        self.workspace.setWorkspace(ws)

    def saveApplicationState(self):
        # The geometry of the window and current workspace
        # are saved in the main configuration file.
        self.settings.setValue("main::geometry", self.saveGeometry());
        self.settings.setValue("main::workspace", self.workspace.activeWorkspace())
        
        self.workspace.saveState()

class WorkspaceManager(QtCore.QObject):
    
    Defaults = ["Wrangler", "Systems", "Artist", "Production"]

    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)
        # Fix later
        self.__active = None
        self.__settings = None
        self.__panel_types = { }
        self.__panels = []
        self.__workspaces = list(self.Defaults)

        # TODO: a more official way to register panels.
        self.registerPanelType("Render Watch", RenderJobWatchPanel)
        self.registerPanelType("Tasks", TaskPanel)
        self.registerPanelType("Clusters", ClusterPanel)
        self.registerPanelType("Nodes", NodePanel)
        self.registerPanelType("Logs", LogsPanel)

        EventManager.bind("GLOBAL_REFRESH", self.refresh)

    def activeWorkspace(self):
        return self.__active

    def registerPanelType(self, ptype, klass):
        if self.__panel_types.has_key(ptype):
            return
        self.__panel_types[ptype] = klass

    def settings(self):
        return self.__settings

    def refresh(self):
        for panel in self.__panels:
            panel.refresh()

    def addPanelCreationMenu(self, obj):
        menu = obj.addMenu("Panels")
        for ptype in self.__panel_types.keys():
            a = menu.addAction(ptype)
        menu.triggered.connect(self.__panelMenuTriggered)

    def addWorkspaceSelectionMenu(self, obj):
        menu = QtGui.QMenu(obj)
        [menu.addAction(s) for s in self.__workspaces]
        # TODO add custom workspaces
        menu.addSeparator()
        menu.addAction("Reset")
        menu.addAction("New Workspace")
        menu.triggered.connect(self.__menuItemTriggered)

        self.btn = QtGui.QToolButton(obj)
        self.btn.setFocusPolicy(QtCore.Qt.NoFocus)
        self.btn.setText("")
        self.btn.setPopupMode(QtGui.QToolButton.InstantPopup)
        self.btn.setMenu(menu)
        obj.addWidget(self.btn)

    def createPanel(self, ptype, name=None, restore=True):
        klass = self.__panel_types[ptype]
        p = klass(name or ptype, self.parent())
        p.restore(self.__settings)
        p.panelClosed.connect(self.__panelClosed)
        self.__panels.append(p)
        if restore:
            self.parent().restoreDockWidget(p)
        else:
            self.parent().addDockWidget(QtCore.Qt.TopDockWidgetArea, p)
        return p

    def saveState(self, close=False):
        if not self.__settings:
            return

        self.__settings.setValue("main::windowState", self.parent().saveState());
        self.__settings.setValue("main::openPanelNames", [p.name() for p in self.__panels])
        self.__settings.setValue("main::openPanelTypes", [p.type() for p in self.__panels])
        
        for panel in self.__panels:
            panel.save(self.__settings)
            if close:
                self.parent().removeDockWidget(panel)
        del self.__panels[0:len(self.__panels)]
        self.__settings.sync()

    def setWorkspace(self, name):
        if name == self.__active:
            return

        if self.__settings:
            self.saveState(close=True)

        self.__active = name
        self.__settings = QtCore.QSettings("plow", "ws_%s" % name)
        self.btn.setText(name)

        winstate = self.__settings.value("main::windowState")
        if winstate:
            self.parent().restoreState(winstate)

        panelNames = self.__settings.value("main::openPanelNames")
        panelTypes = self.__settings.value("main::openPanelTypes")
        if panelNames:
            for name, ptype in zip(panelNames, panelTypes):
                p = self.createPanel(ptype, name, True)
                self.parent().restoreDockWidget(p)
                p.show()

    def __menuItemTriggered(self, action):
        name = action.text()
        if name in self.__workspaces:
            self.setWorkspace(name)

    def __panelClosed(self, panel):
        self.parent().removeDockWidget(panel)
        self.__panels.remove(panel)   

    def __panelMenuTriggered(self, action):
        ptype = str(action.text())
        self.createPanel(ptype, None, False)

def launch(argv, name, layout=None):
    # Initialize the default configuration files if none exist

    app = QtGui.QApplication(argv)
    app.setStyle("plastique")
    app.setStyleSheet(open(os.path.dirname(__file__) + "/resources/style.css").read())

    win = MainWindow(name, layout)
    app.lastWindowClosed.connect(win.saveApplicationState)
    win.show()
    app.exec_()


def main():
    import signal
    import sys
    signal.signal(signal.SIGINT, signal.SIG_DFL)
    launch(sys.argv, "Plow Wrangle", "Wrangler")

if __name__ == "__main__":
    main()

