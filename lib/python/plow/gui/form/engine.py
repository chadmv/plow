import os

from PySide import QtCore, QtGui

import fwidgets

class FormWidgetFactory(object):

    Map = {
        "text": fwidgets.Text,
        "number": fwidgets.Number,
        "datetime": fwidgets.DateTime,
        "duration": fwidgets.Duration
    }

    @classmethod
    def create(cls, data, parent=None):
        widget = data.get("widget")
        value = data.get("value")
        if not widget:
            if isinstance(value, int):
                widget = "number"
            else:
                widget = "text"

        w = cls.Map[widget](data.get("value", ""), parent)
        w.setReadOnly(data.get("readOnly", False))
        return w

    @classmethod
    def register(cls, name, klass):
        cls.Map[name] = klass

class RotatingTriangle(QtGui.QWidget):
    """
    An triagle image that can be toggled to the left and
    down position.
    """

    __OPEN_PIX = None
    __CLOSE_PIX = None

    def __init__(self, opened=True, parent=None):
        QtGui.QWidget.__init__(self, parent)

        if not RotatingTriangle.__OPEN_PIX:
            RotatingTriangle.__OPEN_PIX = QtGui.QPixmap(":/images/down_arrow.png")
            RotatingTriangle.__CLOSE_PIX = self.__rotate(-90, self.__OPEN_PIX)

        self.__open = opened
        if self.__open:
            self.displayOpen()
        else: 
            self.displayClosed()

    def paintEvent(self, event):
        painter = QtGui.QPainter()
        painter.begin(self)
        painter.setRenderHints(
            painter.HighQualityAntialiasing |
            painter.SmoothPixmapTransform |
            painter.Antialiasing)
        painter.translate(8, 9)
        painter.drawPixmap(-self.__pix.width()/2, -self.__pix.height()/2, self.__pix)
        painter.end()

    def __rotate(self, degrees, pix):
        rm = QtGui.QMatrix()
        rm.rotate(degrees)
        return pix.transformed(rm, QtCore.Qt.SmoothTransformation)

    def displayClosed(self):
        self.__pix = RotatingTriangle.__CLOSE_PIX
        self.__open = False

    def displayOpen(self):
        self.__pix = RotatingTriangle.__OPEN_PIX
        self.__open = True

    def toggle(self):
        if self.__open:
            self.displayClosed()
        else:
            self.displayOpen()


class PlowFormGroupHeader(QtGui.QFrame):
    """
    A rounded pill header for the form group.
    """
    def __init__(self, title, parent=None):
        QtGui.QFrame.__init__(self, parent)
        QtGui.QHBoxLayout(self)
        self.setObjectName("PlowFormHeader")

        self.setStyleSheet("QFrame#PlowFormHeader { border: 1px solid #222222; background-color: #2a2a2a; border-radius: 6px; }")
        self.setMaximumHeight(26)
        self.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Fixed)

        self.label = QtGui.QLabel(title, self)
        self.label.setStyleSheet("background-color: #2a2a2a;")

        self.button = RotatingTriangle(True, self)
        self.button.setMaximumWidth(18)

        self.layout().addWidget(self.button)
        self.layout().addWidget(self.label)
        self.layout().setSpacing(0)
        self.layout().setContentsMargins(2, 2, 2, 2)

    def toggle(self):
        self.button.toggle()

class PlowFormGroup(QtGui.QWidget):
    """
    Contains all the elements of a form group.
    """
    def __init__(self, title, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        self.setMouseTracking(True)
        layout.setContentsMargins(0, 0, 0, 0)

        self.setStyleSheet("margin: 0px; padding: 0px;")

        self.__formWidget = QtGui.QWidget(self)
        self.__formLayout = QtGui.QFormLayout(self.__formWidget)
        self.__formLayout.setContentsMargins(0, 0, 0, 0)

        if title:
            self.__header = PlowFormGroupHeader(title, self)
            layout.addWidget(self.__header)

        layout.addWidget(self.__formWidget)

    def addFormWidget(self, child):
        w = FormWidgetFactory.create(child, self)
        self.__formLayout.addRow(child.get("title", "Untitled") + ":", w)

    def addFormGroup(self, group):
        self.__formLayout.addWidget(group)

    def mouseDoubleClickEvent(self, event):
        if self.__header.geometry().contains(event.pos()):
            self.__formWidget.setVisible(not self.__formWidget.isVisible())
            self.__header.toggle()


class PlowForm(QtGui.QWidget):
    """
    The root form container. 
    """
    def __init__(self, form, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)
        
        self.__root = PlowFormGroup(None, self)
        self.__form = form
        [self.__processForm(item, None) for item in form]

        layout.addWidget(self.__root)
        layout.setContentsMargins(4, 4, 4, 4)
        layout.addStretch()

    def __processForm(self, item, parent=None):
        if item.get("children"):
            new_group = PlowFormGroup(item.get("title", "Untitled"), self)
            if parent:
                parent.addFormGroup(new_group)
            else:
                self.layout().addWidget(new_group)
            for child in item.get("children"):
                self.__processForm(child, new_group)
        else:
            if not parent:
                self.__root.addFormWidget(item)
            else:
                parent.addFormWidget(item)

















