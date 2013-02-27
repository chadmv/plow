"""Commonly used Job widgets."""

from plow.gui.manifest import QtCore, QtGui

from plow.gui.constants import COLOR_TASK_STATE

class JobProgressBar(QtGui.QWidget):
    # Left, top, right, bottom
    Margins = [5, 2, 10, 4]
    LeftMargin = 5
    RightMargin = 10;

    def __init__(self, job, parent=None):
        QtGui.QWidget.__init__(self, parent)
        self.__job = job

    def paintEvent(self, event):

        total_width = self.width()
        total_height = self.height()
        total_tasks = float(self.__job.totals.totalTaskCount)

        widths = [
            self.__job.totals.waitingTaskCount / total_tasks,
            self.__job.totals.runningTaskCount / total_tasks,
            self.__job.totals.deadTaskCount / total_tasks,
            self.__job.totals.eatenTaskCount / total_tasks,
            self.__job.totals.dependTaskCount / total_tasks,
            self.__job.totals.succeededTaskCount / total_tasks
        ]

        rects = [ QtCore.QRectF(self.Margins[0], self.Margins[1],
            (total_width-self.Margins[2])* w, total_height - self.Margins[3]) for w in widths ]

        painter = QtGui.QPainter()
        painter.begin(self)
        painter.setPen(QtCore.Qt.NoPen)

        palette = QtGui.QPalette()
        brush = QtGui.QBrush(QtCore.Qt.SolidPattern)

        for i, rect in enumerate(rects):
            if i > 0:
                rect.moveLeft(rects[i-1].right())
            brush.setColor(COLOR_TASK_STATE[i + 1])
            painter.fillRect(rect, brush)
        
        painter.end();
