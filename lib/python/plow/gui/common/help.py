import os

from plow.gui.manifest import QtGui, QtCore

class HelpData(object):
    Data = { }

    @classmethod
    def loadHelp(cls):
        
        tag = None
        text = []

        path = os.path.dirname(os.path.dirname(__file__)) + "/resources/help.txt"
        fp = open(path, "r")
        try:
            for line in fp.xreadlines():
                if line.find("---") == 0:
                    if tag is not None:
                        cls.Data[tag] = text
                    tag = None
                    text = list()
                elif tag is None:
                    tag = line.strip()
                elif tag is not None:
                    text.append(line.strip())
        finally:
            fp.close()

def getHelp(tag):
    if not HelpData.Data:
        HelpData.loadHelp()
    return HelpData.Data.get(tag.upper(), "")

def getHelpTextWidget(tag):
    text = QtGui.QTextEdit()
    text.setFocusPolicy(QtCore.Qt.NoFocus)
    text.setReadOnly(True)
    text.setAutoFormatting(text.AutoAll)
    [text.append(t) for t in getHelp(tag)]
    return text




