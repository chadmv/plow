import os
import logging

import plow.client

from plow.gui.manifest import QtCore, QtGui
from plow.gui.panels import Panel
from plow.gui.event import EventManager

LOGGER = logging.getLogger(__file__)


class LogsPanel(Panel):

    def __init__(self, name="Logs", parent=None):
        Panel.__init__(self, name, "Logs", parent)

        self.setWidget(TabbedLogVieweWidget(self.attrs, self))
        self.setWindowTitle(name)

        EventManager.bind("TASK_OF_INTEREST", self.__handleTaskOfInterestEvent)

    def init(self):
        pass

    def refresh(self):
        pass

    def _openPanelSettingsDialog(self):
        pass

    def __handleTaskOfInterestEvent(self, *args, **kwargs):
        task = plow.client.get_task(args[0])
        job = plow.client.get_job(args[1])

        self.widget().addTask(job, task)


class TabbedLogVieweWidget(QtGui.QWidget):
    def __init__(self, attrs, parent=None):
        QtGui.QWidget.__init__(self, parent)

        self.__tasks = {}
        self.__interval = 15000

        self.__tabs = QtGui.QTabWidget(self)
        self.__tabs.setTabsClosable(True)
        self.__tabs.tabCloseRequested.connect(self.closeTab)

        # self.__txt_search = QtGui.QLineEdit(self)
        # self.__btn_find = QtGui.QPushButton("Find", self)
        # self.__btn_find_prev = QtGui.QPushButton("Find Prev", self)
        # self.__btn_find_all = QtGui.QPushButton("Find All", self)

        # low_layout = QtGui.QHBoxLayout()
        # low_layout.addWidget(self.__txt_search)
        # low_layout.addWidget(self.__btn_find)
        # low_layout.addWidget(self.__btn_find_prev)
        # low_layout.addWidget(self.__btn_find_all)

        layout = QtGui.QVBoxLayout()
        layout.addWidget(self.__tabs)
        # layout.addLayout(low_layout)
        self.setLayout(layout)

    def addTask(self, job, task):
        index = self.__tasks.get(task.id, None)
        if index is not None:
            self.__tabs.setCurrentIndex(index)
        else:
            viewer = LogViewerWidget(job, task, {}, self)
            index = self.__tabs.addTab(viewer, task.name)
            self.__tabs.setTabToolTip(index, viewer.logPath)
            self.__tabs.setCurrentIndex(index)
            self.__tasks[task.id] = index
            
    def closeTab(self, index):
        taskId = None
        for k, v in self.__tasks.iteritems():
            if v == index:
                taskId = k
                break
        if taskId:
            del self.__tasks[taskId]
        self.__tabs.removeTab(index)

    def closeAllTabs(self):
        while self.__tabs.count():
            self.closeTab(0) 

    def interval(self):
        return self.__interval

    def setInterval(self, msec):
        self.__interval = msec
        for i in xrange(self.__tabs.count()):
            self.__tabs.widget(i).setInterval(msec)


class LogViewerWidget(QtGui.QWidget):

    def __init__(self, job=None, task=None, attrs=None, parent=None):
        QtGui.QWidget.__init__(self, parent)

        self.__task = None 
        self.__log_file = QtCore.QFile()
        self.__log_stream = QtCore.QTextStream()

        openAction = QtGui.QAction("Open Log File", self)
        openAction.setShortcut(QtGui.QKeySequence.Open)
        self.addAction(openAction)

        self.__searchLine = QtGui.QLineEdit(self)
        self.__chk_tail = QtGui.QAction("Tail log", self)
        self.__chk_tail.setCheckable(True)

        self.__findPrevBtn = prev = QtGui.QAction(self)
        prev.setToolTip("Find Previous Match")
        prev.setIcon(QtGui.QIcon(":/left_arrow.png"))

        self.__findNextBtn = nxt = QtGui.QAction(self)
        nxt.setToolTip("Find Next Match")
        nxt.setIcon(QtGui.QIcon(":/right_arrow.png"))

        self.__jobNameLabel = label = QtGui.QLabel(self)
        label.setIndent(10)
        # label.setTextInteractionFlags(QtCore.Qt.TextSelectableByMouse)
        label.hide()
        self.__jobNameSpacer = QtGui.QWidget(self)
        self.__jobNameSpacer.setFixedHeight(6)
        self.__jobNameSpacer.hide()

        def spacer(width):
            w = QtGui.QWidget()
            w.setFixedWidth(width)
            return w

        stretch = QtGui.QWidget()
        stretch.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Preferred)

        self.__toolbar = tb = QtGui.QToolBar(self)
        tb.addWidget(spacer(4))
        tb.addWidget(QtGui.QLabel("Find: ", self))
        tb.addWidget(self.__searchLine)
        tb.addAction(self.__findPrevBtn)
        tb.addAction(self.__findNextBtn)
        tb.addWidget(stretch)
        tb.addAction(self.__chk_tail)
        tb.addWidget(spacer(4))

        self.__view = view = QtGui.QPlainTextEdit(self)
        font = view.font()
        font.setPointSize(font.pointSize()-2)
        font.setWeight(font.Light)
        view.setFont(font)
        view.setLineWrapMode(view.WidgetWidth)
        view.setReadOnly(True)
        view.setMaximumBlockCount(1000000)
        self.__view.setFocusPolicy(QtCore.Qt.NoFocus)

        layout = QtGui.QVBoxLayout(self)
        layout.setSpacing(0)
        layout.setContentsMargins(0,0,0,0)
        layout.addWidget(self.__toolbar)
        layout.addWidget(self.__jobNameLabel)
        layout.addWidget(self.__jobNameSpacer)
        layout.addWidget(self.__view)

        self.__highlighter = TextHighlighter(view.document())
        self.__logWatcher = FileWatcher(self)

        # Connections
        self.__logWatcher.fileChanged.connect(self.__logUpdated)
        self.__chk_tail.toggled.connect(self.__logTailToggled)
        self.__searchLine.textChanged.connect(self.findText)
        self.__searchLine.returnPressed.connect(self.findNext)
        self.__findPrevBtn.triggered.connect(self.findPrev)
        self.__findNextBtn.triggered.connect(self.findNext)
        openAction.triggered.connect(self.openLogFile)

        # Optional args
        if job:
            self.setJobName(job.name)

        if task:
            self.setCurrentTask(task)


    @property 
    def logPath(self):
        return self.__log_file.fileName() 

    @property 
    def taskId(self):
        if self.__task:
            return self.__task.id

    @property 
    def taskName(self):
        if self.__task:
            return self.__task.name 

    @property 
    def task(self):
        return self.__task

    @property 
    def jobName(self):
        return self.__jobNameLabel.text()

    def interval(self):
        return self.__logWatcher.interval()

    def setInterval(self, msec):
        self.__logWatcher.setInterval(msec)

    def setCurrentTask(self, task):
        if not task.id or task.id == self.taskId:
            return 

        logPath = task.get_log_path()
        if not os.path.exists(logPath):
            LOGGER.warn("Failed to open log file: '%s'", logPath)
            return

        self.__task = task
        self.setLogPath(logPath)

    def setLogPath(self, path):
        self.stopLogTail()

        f = self.__log_file
        f.close()
        f.setFileName(path)
        if not f.open(QtCore.QIODevice.ReadOnly | QtCore.QIODevice.Text):
            LOGGER.warn("Failed to open log file '%s'", path)
            return

        self.__view.clear()

        self.__log_stream.setDevice(f)
        self.__view.setPlainText(self.__log_stream.readAll())

        if self.__chk_tail.isChecked():
            self.startLogTail()


    def setJobName(self, name):
        self.__jobNameLabel.setText(name)
        self.__jobNameLabel.setVisible(bool(name))
        self.__jobNameSpacer.setVisible(bool(name))

    def findText(self, text, cursor=None, opts=0):
        if not cursor:
            cursor = QtGui.QTextCursor()
        newCursor = self.__view.document().find(text, cursor, opts)
        if newCursor.isNull():
            LOGGER.debug("nothing found in text")

        self.__view.setTextCursor(newCursor)
        self.__view.centerCursor()

        self.__highlighter.setFoundMatchText(text)

    def findPrev(self):
        cursor = self.__view.textCursor()
        self.findText(self.__searchLine.text(), cursor, QtGui.QTextDocument.FindBackward)

    def findNext(self):
        cursor = self.__view.textCursor()
        self.findText(self.__searchLine.text(), cursor) 

    def startLogTail(self):
        self.stopLogTail()
        self.__logWatcher.addPath(self.__log_file.fileName()) 

    def stopLogTail(self):
        paths = self.__logWatcher.files()
        if paths:
            self.__logWatcher.removePaths(paths) 

    def openLogFile(self):
        logPath, _ = QtGui.QFileDialog.getOpenFileName(self,
                                                        "Open a log file",
                                                        "",
                                                        "Logs (*.log *.txt);;All Files (*)")

        if logPath:
            self.setLogPath(logPath)

    def __logUpdated(self):
        cursor = self.__view.textCursor()
        cursor.movePosition(cursor.End)

        while True:
            line = self.__log_stream.read(1024)
            if not line:
                break
            cursor.insertText(line) 

        if not self.__searchLine.text().strip():
            bar = self.__view.verticalScrollBar()
            bar.setValue(bar.maximum())

    def __logTailToggled(self, checked):
        if checked:
            self.startLogTail()
        else:
            self.stopLogTail()

 
class FileWatcher(QtCore.QObject):

    fileChanged = QtCore.Signal(str)

    def __init__(self, parent=None):
        super(FileWatcher, self).__init__(parent)

        self.__files = {}

        self.__timer = QtCore.QTimer(self)
        self.__timer.setInterval(5000)
        self.__timer.timeout.connect(self.checkFiles)

    def addPath(self, path):
        if not path in self.__files:
            self.__files[path] = QtCore.QFileInfo(path).lastModified()

        if self.__files and not self.__timer.isActive():
            self.start() 

    def removePath(self, path):
        try:
            del self.__files[path]
        except KeyError:
            pass

        if not self.__files:
            self.stop()

    def removePaths(self, paths):
        for p in paths:
            self.removePath(p)

    def interval(self):
        return self.__timer.interval()

    def setInterval(self, msec):
        timer = self.__timer
        timer.setInterval(msec)
        if timer.isActive():
            timer.start()

    def start(self):
        self.__timer.start()

    def stop(self):
        self.__timer.stop()

    def files(self):
        return self.__files.keys()

    def checkFiles(self):
        if not self.__files:
            return 

        info = QtCore.QFileInfo()

        for path, mtime in self.__files.iteritems():
            info.setFile(path)
            test_mtime = info.lastModified()
            if mtime != test_mtime:
                self.__files[path] = test_mtime
                LOGGER.debug("Log file modified: (%r) '%s'", test_mtime, path)
                self.fileChanged.emit(path)


class TextHighlighter(QtGui.QSyntaxHighlighter):

    def __init__(self, parent):
        super(TextHighlighter, self).__init__(parent)

        self.__highlightingRules = []

        fmt = QtGui.QTextCharFormat()
        fmt.setForeground(QtCore.Qt.white)
        fmt.setBackground(QtCore.Qt.darkGreen)
        pattern = QtCore.QRegExp("", QtCore.Qt.CaseInsensitive)
        self.__foundMatchFormat = (fmt, pattern)

        fmt = QtGui.QTextCharFormat()
        fmt.setForeground(QtGui.QColor(QtCore.Qt.red).lighter(115))
        errors = ["error", "critical", "failed", "fail", "crashed", "crash"]
        for pattern in errors:
            rx = QtCore.QRegExp(r'\b%s\b' % pattern, QtCore.Qt.CaseInsensitive)
            rule = (fmt, rx)
            self.__highlightingRules.append(rule)

        fmt = QtGui.QTextCharFormat()
        fmt.setForeground(QtGui.QColor(255,168,0))
        for pattern in ("warning", "warn"):
            rx = QtCore.QRegExp(r'\b%s\b' % pattern, QtCore.Qt.CaseInsensitive)
            rule = (fmt, rx)
            self.__highlightingRules.append(rule)


    @QtCore.Slot(str)
    def setFoundMatchText(self, text):
        self.__foundMatchFormat[1].setPattern(text)
        self.rehighlight()

    def highlightBlock(self, text):
        for fmt, pattern in self.__highlightingRules:
            expression = QtCore.QRegExp(pattern)
            index = expression.indexIn(text)
            while index >= 0:
                length = expression.matchedLength()
                self.setFormat(index, length, fmt) 
                index = expression.indexIn(text, index + length)

        fmt, pattern = self.__foundMatchFormat
        if pattern.isEmpty():
            return 

        expression = QtCore.QRegExp(pattern)
        index = expression.indexIn(text)
        while index >= 0:
            length = expression.matchedLength()
            self.setFormat(index, length, fmt)
            index = expression.indexIn(text, index + length)


if __name__ == "__main__":
    app = QtGui.QApplication([])
    
    app.setStyle("plastique")
    app.setStyleSheet(open(os.path.dirname(__file__) + "/../resources/style.css").read())

    l = LogViewerWidget()
    l.resize(800,600)
    l.show()
    app.exec_()
