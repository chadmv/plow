
from plow.gui.manifest import QtCore, QtGui

class PanelManager(QtCore.QObject):
    def __init__(self, parent):
        QtCore.QObject.__init__(self, parent)

class Panel(QtGui.QDockWidget):
    """
    The base class for all panels.
    """
    def __init__(self, name, parent=None):
        QtGui.QDockWidget.__init__(self, parent)

        # Add the standard dock action buttons in.
        # TODO: hook up signals
        self.__label = QtGui.QLabel(self)
        self.__name = None
        self.setName(name)

        # Note: the widet in the panel adds more buttons
        # to this toolbar.
        toolbar = QtGui.QToolBar(self)
        toolbar.setIconSize(QtCore.QSize(18, 18))
        toolbar.addAction(QtGui.QIcon(":/close.png"), "Close")
        toolbar.addAction(QtGui.QIcon(":/float.png"), "Float")
        toolbar.addSeparator()
        self.setTitleBarWidget(toolbar)
        self.init()

        spacer = QtGui.QWidget()
        spacer.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Expanding)

        toolbar.addWidget(spacer)
        toolbar.addWidget(self.__label)

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
        pass

    def restore(self, settings):
        """
        Called when the application needs the panel to restore its configuration.
        """
        pass

    def configKey(self, prop):
        """
        Return the settings key for the given property name.
        """
        return "panel::%s::%s" % (self.objectName(), prop)


