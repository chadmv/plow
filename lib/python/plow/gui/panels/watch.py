"""
The render job watch panel allows you to 
   1. setup filters to automatically load jobs. (defaults to loading your jobs)
   2. individually add jobs you want to watch.
"""

from plow.gui.manifest import QtCore, QtGui
from base import Panel

class RenderJobWatchPanel(Panel):
	def __init__(self, parent=None):
		Panel.__init__(self, parent)

		# Parent will always be the main window
		# So its possible to setup if the search bar
		# should be enbled here.

		widget = RenderJobWatchWidget(self)
		self.setWidget(widget)
		self.setWindowTitle("Job Watcher")

		# Might replace titlebar with custom toolbar which
		# has the action icons for the widget

		# TODO: should be icon bar,needs icons
		toolbar = QtGui.QToolBar(self)
		toolbar.addAction("Foo")
		self.setTitleBarWidget(toolbar)

class RenderJobWatchWidget(QtGui.QWidget):
	def __init__(self, parent=None):
		QtGui.QWidget.__init__(self, parent)
		QtGui.QVBoxLayout(self)

		self.__tree = QtGui.QTreeWidget(self)
		self.__tree.setHeaderLabels(["Job", "Status"])

		self.layout().addWidget(self.__tree)


