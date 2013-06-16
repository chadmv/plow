
import os
import logging
import traceback 

from plow.gui.manifest import QtCore, QtGui
from plow.gui import event
from plow.gui import util 

LOGGER = logging.getLogger(__name__)
DEFAULTS_DIR = os.path.join(os.path.abspath(os.path.dirname(__file__)), "defaults")

####################
# WorkspaceManager
####################

class WorkspaceManager(QtCore.QObject):
    
    DEFAULTS = ["Wrangler", "Systems", "Artist", "Production"]

    _PANEL_TYPES = {}
    _DEFAULT_KEYS = ["main::openPanels", "main::windowState"]

    def __init__(self, parent):
        if not isinstance(parent, QtGui.QMainWindow):
            raise TypeError("parent must be a QMainWindow, or subclass")

        super(WorkspaceManager, self).__init__(parent)

        # Fix later
        self.__settings = None
        self.__active = None
        self.__settings = None
        self.__default_settings = None
        self.__panels = set()
        self.__workspaces = self.DEFAULTS

        self.__initDefaultWorkspaces()

        event.EventManager.bind("GLOBAL_REFRESH", self.refresh)

    def __initDefaultWorkspaces(self):
        for space in self.DEFAULTS: 

            name = self.__getWorkspaceConfName(space)
            settings = util.getSettings(name)

            if not settings.contains("main::windowState"):

                src_name = os.path.join(DEFAULTS_DIR, '%s.ini' % name)
                if not os.path.exists(src_name):
                    LOGGER.warn("Could not locate default layout file: %r", src_name)
                    continue

                src = QtCore.QSettings(src_name, QtCore.QSettings.IniFormat)

                for key in self._DEFAULT_KEYS:
                    settings.setValue(key, src.value(key))

                settings.sync()
                settings.deleteLater()


    @classmethod
    def registerPanelType(cls, ptype, klass):
        if cls._PANEL_TYPES.has_key(ptype):
            return
        cls._PANEL_TYPES[ptype] = klass

    def activeWorkspace(self):
        return self.__active

    def settings(self):
        return self.__settings

    def refresh(self):
        for panel in self.__panels:
            panel.refresh()

    def addPanelCreationMenu(self, obj):
        menu = obj.addMenu("Panels")
        for ptype in self._PANEL_TYPES.keys():
            a = menu.addAction(ptype)
        menu.triggered.connect(self.__panelMenuTriggered)

    def addWorkspaceSelectionMenu(self, obj):
        menu = QtGui.QMenu(obj)
        for s in self.__workspaces:
            menu.addAction(s)
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

    def setWorkspace(self, name):
        if name == self.__active:
            return

        LOGGER.debug("Set workspace: %r", name)

        self.saveState(close=True)
        self.__panels.clear()

        self.__active = name
        self.btn.setText(name)

        for s in (self.__settings, self.__default_settings):
            if s is not None:
                s.deleteLater()

        conf_name = self.__getWorkspaceConfName(name)

        self.__settings = util.getSettings(conf_name, parent=self)
        LOGGER.debug("Openning new QSettings file: %r", self.__settings.fileName())

        panels = self.__settings.value("main::openPanels")

        if panels:
            for name, ptype in panels:
                try:
                    p = self.createPanel(ptype, name)  
                except Exception, e:
                    err = traceback.format_exc(3)
                    LOGGER.error("Error creating panel %r (%r): %s", name, ptype, err)

            self.parent().setUpdatesEnabled(False)
            QtCore.QTimer.singleShot(0, self.restoreState)

        else:
            self.restoreState()


    def createPanel(self, ptype, name=None):
        LOGGER.debug("Create panel: %r, %r", ptype, name)
        parent = self.parent()

        klass = self._PANEL_TYPES[ptype]
        p = klass(name or ptype, parent=parent)

        parent.addDockWidget(QtCore.Qt.TopDockWidgetArea, p)

        p.restore(self.__settings)
        p.panelClosed.connect(self.__closePanel)

        self.__panels.add(p)

        return p

    def saveState(self, close=False):
        if not self.__settings:
            return

        parent = self.parent()

        settings = self.__settings
        settings.setValue("main::windowState", parent.saveState());
        settings.setValue("main::openPanels", [(p.name(), p.type()) for p in self.__panels])
        
        for panel in self.__panels.copy():
            panel.save(settings)
            if close:
                self.__closePanel(panel)
        
        settings.sync()

    def restoreState(self):
        winstate = self.__settings.value("main::windowState")
        if winstate:
            self.parent().restoreState(winstate)        
        self.parent().setUpdatesEnabled(True)


    def __menuItemTriggered(self, action):
        name = action.text()
        if name in self.__workspaces:
            self.setWorkspace(name)

    def __closePanel(self, panel):
        try:
            self.__panels.remove(panel) 
        except ValueError: 
            pass  
        self.parent().removeDockWidget(panel)
        panel.widget().deleteLater()
        panel.deleteLater()

    def __panelMenuTriggered(self, action):
        ptype = str(action.text())
        self.createPanel(ptype, None)

    def __getWorkspaceConfName(self, name):
        return "ws_%s" % name


####################
# Panel
####################

class Panel(QtGui.QDockWidget):
    """
    The base class for all panels.
    """

    panelClosed = QtCore.Signal(object)

    def __init__(self, name, ptype, parent=None):
        QtGui.QDockWidget.__init__(self, parent)

        # Add the standard dock action buttons in.
        # TODO: hook up signals
        self.__label = QtGui.QLabel(self)
        self.__label.setIndent(10)
        self.__name = None
        self.__ptype = ptype
        self.setName(name)

        self.attrs = { }

        self.__refreshTimer = None

        # Note: the widet in the panel adds more buttons
        # to this toolbar.
        titleBar = QtGui.QWidget(self)
        barLayout = QtGui.QVBoxLayout(titleBar)
        barLayout.setSpacing(0)
        barLayout.setContentsMargins(0, 0, 0, 0)

        self.__toolbar = toolbar = QtGui.QToolBar(self)
        toolbar.setIconSize(QtCore.QSize(18, 18))
        toolbar.addAction(QtGui.QIcon(":/images/close.png"), "Close", self.__close)

        float_action = QtGui.QAction(QtGui.QIcon(":/images/float.png"), "Float", self)
        float_action.toggled.connect(self.__floatingChanged)
        float_action.setCheckable(True)
        toolbar.addAction(float_action)

        config_action = QtGui.QAction(QtGui.QIcon(":/images/config.png"), "Configure Panel", self)        
        config_action.triggered.connect(self._openPanelSettingsDialog)
        toolbar.addAction(config_action)

        toolbar.addSeparator()

        barLayout.addWidget(self.__label)
        barLayout.addWidget(toolbar)

        self.setTitleBarWidget(titleBar)
        self.init()

    def titleBarWidget(self):
        return self.__toolbar

    def setRefreshTime(self, value):
        value = int(value)
        if self.__refreshTimer is None:
            self.__refreshTimer = QtCore.QTimer(self)
            self.__refreshTimer.timeout.connect(self.refresh)
        if value < 1:
            value = 1
        self.__refreshTimer.stop()
        self.__refreshTimer.start(value * 1000)

    def type(self):
        """
        Return the type of panel.
        """
        return self.__ptype

    def name(self):
        """
        Return the panel's name.
        """
        return self.__name

    def setName(self, name):
        """
        Sets the panel's name, used to allow panels of the same type
        to have unique configurations.
        """
        self.__name = name
        self.__label.setText(name)
        self.setObjectName("%s::%s" % (self.__class__.__name__, self.__name))

    def init(self):
        """
        Initialization function implemented by subclass.
        """
        pass

    def refresh(self):
        """
        Refresh the main widget.
        """
        pass

    def save(self, settings):
        """
        Called when the application needs the planel to save its configuration.
        """
        for attr in self.attrs:
            key = "panel::%s::%s" % (self.objectName(), attr)
            settings.setValue(key, self.attrs[attr])

    def restore(self, settings):
        """
        Called when the application needs the panel to restore its configuration.
        """
        for attr in self.attrs.keys():
            key = "panel::%s::%s" % (self.objectName(), attr)
            if settings.contains(key):
                self.attrs[attr] = settings.value(key)
        
        if self.attrs.has_key("refreshSeconds"):
            val = int(self.attrs["refreshSeconds"])
            self.attrs["refreshSeconds"] = val
            self.setRefreshTime(val)
            QtCore.QTimer.singleShot(0, self.refresh)


    def setAttr(self, prop, value):
        self.attrs[prop] = value

    def getAttr(self, prop, default=None):
        return self.attrs.get(prop, default)

    def _openPanelSettingsDialog(self):
        pass

    def __close(self):
        self.panelClosed.emit(self)

    def __floatingChanged(self, value):
        self.setFloating(value)





