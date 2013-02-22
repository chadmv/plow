"""MainWindow"""

from manifest import QtCore, QtGui

from panels.watch import RenderJobWatchPanel

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

        # Just for testing
        w = RenderJobWatchPanel(self)
        self.addDockWidget(QtCore.Qt.TopDockWidgetArea, w)

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

def launch(name, layout=None):
    # Initialize the default configuration files if none exist

    win = MainWindow(name, layout)
    return win
