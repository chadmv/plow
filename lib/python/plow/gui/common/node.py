
import plow.gui.constants as constants

from plow.gui.manifest import QtCore, QtGui
from plow.gui.form import FormWidget, FormWidgetFactory


_LOCKED_BTN_STYLE = """
QPushButton#Locked { 
    border-top-right-radius: 0px; 
    border-bottom-right-radius: 0px;
    padding: 3px;
    border-right-width: 0px;
}

QPushButton#Locked:checked
{
  background-color: rgba(177, 24, 0, 192) 
}
"""

_UNLOCKED_BTN_STYLE = """
QPushButton#Unlocked { 
    border-top-left-radius: 0px; 
    border-bottom-left-radius: 0px;
    padding: 3px;  
}
QPushButton#Unlocked:checked
{
  background-color: rgba(76, 115, 0, 192) 
}

"""

class LockToggleWidget(QtGui.QWidget):
    def __init__(self, value, parent):
        QtGui.QWidget.__init__(self, parent)

        self.__locked = QtGui.QPushButton("Locked", self)
        self.__locked.setObjectName("Locked")
        self.__locked.setAutoExclusive(True)
        self.__locked.setCheckable(True)
        self.__locked.setStyleSheet(_LOCKED_BTN_STYLE)
        self.__locked.setFocusPolicy(QtCore.Qt.NoFocus)
        self.__unlocked = QtGui.QPushButton("UnLocked", self)
        self.__unlocked.setObjectName("Unlocked")
        self.__unlocked.setAutoExclusive(True)
        self.__unlocked.setCheckable(True)
        self.__unlocked.setStyleSheet(_UNLOCKED_BTN_STYLE)
        self.__unlocked.setFocusPolicy(QtCore.Qt.NoFocus)

        if value:
            self.__locked.setChecked(True)
        else:
            self.__unlocked.setChecked(True)

        l = QtGui.QHBoxLayout(self)
        l.setSpacing(0)
        l.setContentsMargins(0, 0, 0, 0)
        l.addWidget(self.__locked)
        l.addWidget(self.__unlocked)

class LockToggleFormWidget(FormWidget):
    def __init__(self, value, parent=None):
        FormWidget.__init__(self, parent)
        w = LockToggleWidget(value, self)
        self.setWidget(w)


FormWidgetFactory.register("lockToggle", LockToggleFormWidget)

