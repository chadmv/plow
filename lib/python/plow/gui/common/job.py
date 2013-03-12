"""Commonly used Job widgets."""

import plow.client

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


class JobSelectionDialog(QtGui.QDialog):
    def __init__(self, parent=None):
        QtGui.QDialog.__init__(self, parent)
        self.__jobs = plow.client.get_jobs()

        self.__txt_filter = QtGui.QLineEdit(self)
        self.__txt_filter.textChanged.connect(self.__filterChanged)
        self.__list_jobs = QtGui.QListWidget(self)
        self.__list_jobs.setSelectionMode(self.__list_jobs.ExtendedSelection)
        self.__list_jobs.addItems([job.name for job in self.__jobs])
        self.__list_jobs.sortItems()

        layout = QtGui.QVBoxLayout()
        layout.addWidget(self.__txt_filter)
        layout.addWidget(self.__list_jobs)
        self.setLayout(layout)

    def __filterChanged(self, value):
        if not value:
            self.__list_jobs.clear()
            self.__list_jobs.addItems([job.name for job in self.__jobs])
        else:
            new_items = [i.text() for i in self.__list_jobs.findItems(value, QtCore.Qt.MatchContains)]
            self.__list_jobs.clear()
            self.__list_jobs.addItems(new_items)
        self.__list_jobs.sortItems()






