
import os
import time
from datetime import datetime

from manifest import QtGui
from plow.tools.util import *


def loadTheme():
    QtGui.QApplication.setStyle("plastique")
    path = os.path.join(os.path.dirname(__file__) , "resources/style.css")
    QtGui.qApp.setStyleSheet(open(path).read())


def ask(msg, title='Confirm?', parent=None):
    ret = QtGui.QMessageBox.question(parent, title, msg,
                                     QtGui.QMessageBox.Ok | QtGui.QMessageBox.Cancel,
                                     QtGui.QMessageBox.Cancel )
   
    return ret == QtGui.QMessageBox.Ok
