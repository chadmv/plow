
from plow.gui.manifest import QtCore, QtGui

class PanelManager(QtCore.QObject):
    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)


class Panel(QtGui.QDockWidget):
    """
    The base class for all panels.
    """

    def __init__(self, name, ptype, parent=None):
        QtGui.QDockWidget.__init__(self, parent)

        # Add the standard dock action buttons in.
        # TODO: hook up signals
        self.__label = QtGui.QLabel(self)
        self.__name = None
        self.__ptype = ptype
        self.setName(name)

        self.attrs = { }

        self.__refreshTimer = None

        # Note: the widet in the panel adds more buttons
        # to this toolbar.
        toolbar = QtGui.QToolBar(self)
        toolbar.setIconSize(QtCore.QSize(18, 18))
        toolbar.addAction(QtGui.QIcon(":/close.png"), "Close", self.__close)

        float_action = QtGui.QAction(QtGui.QIcon(":/float.png"), "Float", self)
        float_action.toggled.connect(self.__floatingChanged)
        float_action.setCheckable(True)
        toolbar.addAction(float_action)

        config_action = QtGui.QAction(QtGui.QIcon(":/config.png"), "Configure Panel", self)        
        config_action.triggered.connect(self._openPanelSettingsDialog)
        toolbar.addAction(config_action)

        toolbar.addSeparator()
        self.setTitleBarWidget(toolbar)
        self.init()

        spacer = QtGui.QWidget()
        spacer.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Expanding)

        toolbar.addWidget(spacer)
        toolbar.addWidget(self.__label)

    def setRefreshTime(self, value):
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
            self.setRefreshTime(self.attrs["refreshSeconds"])
            self.refresh()


    def setAttr(self, prop, value):
        self.attrs[prop] = value

    def getAttr(self, prop, default=None):
        return self.attrs.get(prop)

    def _openPanelSettingsDialog(self):
        pass

    def __close(self):
        self.panelClosed.emit(self)

    def __floatingChanged(self, value):
        self.setFloating(value)

Panel.panelClosed = QtCore.Signal(Panel)




