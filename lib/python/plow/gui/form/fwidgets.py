from plow.gui.manifest import QtCore, QtGui

__all__ = [
    "Text",
    "Number",
]

class FormWidget(QtGui.QWidget):
    """
    The base class for all form widgets.
    """
    __LOCKED_PIX = None

    def __init__(self, value, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QGridLayout(self)
        self.layout().setSpacing(0)
        self.layout().setContentsMargins(0, 0, 0, 0)
        self._widget = None

        self.__status = QtGui.QLabel(self)
        self.__status.setContentsMargins(5, 0, 0, 0)
        self.layout().addWidget(self.__status, 0, 1)

        if not FormWidget.__LOCKED_PIX:
            FormWidget.__LOCKED_PIX = QtGui.QPixmap(":/locked.png")
            FormWidget.__LOCKED_PIX = FormWidget.__LOCKED_PIX.scaled(
                QtCore.QSize(12, 12), QtCore.Qt.KeepAspectRatio, QtCore.Qt.SmoothTransformation)

    def setReadOnly(self, value):
        self._setReadOnly(value)
        if value:
            self.__status.setPixmap(FormWidget.__LOCKED_PIX)
        else:
            self.__status.setText("")

    def _setReadOnly(self, value):
        pass

    def setWidget(self, widget):
        self._widget = widget
        self.layout().addWidget(widget, 0, 0)


class Text(FormWidget):
    def __init__(self, text, parent=None):
        FormWidget.__init__(self, parent)
        self.setWidget(QtGui.QLineEdit(text, self))

    def _setReadOnly(self, value):
        self._widget.setReadOnly(value)

class Number(FormWidget):
    def __init__(self, value, parent=None):
        FormWidget.__init__(self, parent)
        widget = QtGui.QSpinBox(self)
        widget.setValue(value)
        self.setWidget(widget)

    def _setReadOnly(self, value):
        self._widget.setReadOnly(value)