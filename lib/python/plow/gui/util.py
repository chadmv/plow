
import os
import time
from datetime import datetime

from manifest import QtCore, QtGui
from plow.tools.util import *
from plow.gui.dialogs.util import ask


def loadTheme():
    QtGui.QApplication.setStyle("plastique")
    path = os.path.join(os.path.dirname(__file__) , "resources/style.css")
    QtGui.qApp.setStyleSheet(open(path).read())


def getSettings(name, scope=QtCore.QSettings.UserScope, parent=None):
    settings = QtCore.QSettings(QtCore.QSettings.NativeFormat, 
                                scope,
                                "plow", 
                                name, 
                                parent)
    return settings


def copyToClipboard(item):
    """
    Handle the copying of different item types 
    to the global clipboard. 
    """
    clipboard = QtGui.QApplication.clipboard()

    if isinstance(item, QtGui.QPixmap):
        action = clipboard.setPixmap 
    elif isinstance(item, QtGui.QImage):
        action = clipboard.setImage 
    elif isinstance(item, QtCore.QMimeData):
        action = clipboard.setMimeData 
    else:
        action = clipboard.setText 
        item = str(item)

    action(item, clipboard.Selection)
    action(item, clipboard.Clipboard)