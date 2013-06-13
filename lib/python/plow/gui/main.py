"""MainWindow"""
import os
import logging 

from manifest import QtCore, QtGui
from util import loadTheme 
from panels import *


LOGGER = logging.getLogger("plow-wrangler")


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

        self.resize(1024,800)

        self.__default_workspace = workspace
        self.setDockOptions(self.AnimatedDocks | 
                            self.AllowNestedDocks |
                            self.AllowTabbedDocks |
                            self.VerticalTabs)

        self.settings = QtCore.QSettings("plow", appname)
        self.workspace = WorkspaceManager(self)

        LOGGER.debug("Using settings file %s", self.settings.fileName())

        spacer = QtGui.QWidget()
        spacer.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Expanding)

        # Only has workspace and search
        self.toolbar_top = QtGui.QToolBar(self)
        self.toolbar_top.setObjectName("Toolbar")
        self.toolbar_top.addWidget(spacer)
        self.toolbar_top.toggleViewAction().setDisabled(True)
        self.workspace.addWorkspaceSelectionMenu(self.toolbar_top)

        self.addToolBar(QtCore.Qt.TopToolBarArea, self.toolbar_top)

        # Setup menu bar
        menubar = QtGui.QMenuBar()
        self.workspace.addPanelCreationMenu(menubar)

        menu_window = menubar.addMenu("Window")
        menu_window.addAction("New Window")
        menu_window.addSeparator()

        self.setMenuBar(menubar)
        QtCore.QTimer.singleShot(0, self.restoreApplicationState)

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




def launch(argv, name, layout=None):
    # Initialize the default configuration files if none exist
    app = QtGui.QApplication(argv)
    app.setAttribute(QtCore.Qt.AA_DontShowIconsInMenus, False)
    loadTheme()

    win = MainWindow(name, layout)
    app.lastWindowClosed.connect(win.saveApplicationState)
    win.show()
    win.raise_()
    app.exec_()


def main():
    import signal
    import sys
    import argparse

    parser = argparse.ArgumentParser(
        description='Plow Monitoring and Management GUI',
        usage='%(prog)s [opts]',
    )

    parser.add_argument("-debug", action="store_true", 
        help="Print more debugging output")

    args = parser.parse_args()  

    logging.basicConfig(level=logging.DEBUG if args.debug else logging.INFO)

    signal.signal(signal.SIGINT, signal.SIG_DFL)
    launch(sys.argv, "Plow Wrangle", "Wrangler")


if __name__ == "__main__":
    main()

