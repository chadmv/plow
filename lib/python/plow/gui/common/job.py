"""Commonly used Job widgets."""

from plow.gui.manifest import QtCore, QtGui

from plow.gui.constants import COLOR_TASK_STATE

class JobProgressBar(QtGui.QWidget):
    # Left, top, right, bottom
    __PEN = QtGui.QColor(33, 33, 33)

    Margins = [5, 2, 10, 4]

    def __init__(self, totals, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.__totals = totals

    def setTotals(self, totals):
        self.__totals = totals

    def paintEvent(self, event):

        total_width = self.width()
        total_height = self.height()
        total_tasks = float(self.__totals.totalTaskCount)

        widths = [
            self.__totals.waitingTaskCount / total_tasks,
            self.__totals.runningTaskCount / total_tasks,
            self.__totals.deadTaskCount / total_tasks,
            self.__totals.eatenTaskCount / total_tasks,
            self.__totals.dependTaskCount / total_tasks,
            self.__totals.succeededTaskCount / total_tasks
        ]

        rects = [ QtCore.QRectF(self.Margins[0], self.Margins[1],
            (total_width-self.Margins[2])* w, total_height - self.Margins[3]) for w in widths ]

        painter = QtGui.QPainter()
        painter.begin(self)
        painter.setRenderHints(
            painter.HighQualityAntialiasing |
            painter.SmoothPixmapTransform |
            painter.Antialiasing)

        painter.setPen(self.__PEN)

        for i, rect in enumerate(rects):
            if i > 0:
                rect.moveLeft(rects[i-1].right())
            painter.setBrush(COLOR_TASK_STATE[i + 1])
            painter.drawRoundedRect(rect, 3, 3)
        
        painter.end();
