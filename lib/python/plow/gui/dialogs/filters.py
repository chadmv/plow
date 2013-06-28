
import logging 
import cPickle 
from functools import partial 

from plow import client 
from plow.client import DependType

from plow.gui.manifest import QtCore, QtGui
from plow.gui import util 

LOGGER = logging.getLogger(__name__)


########################
# FilterManager
#
class FilterManager(QtGui.QDialog):
    """

    """
    def __init__(self, project, *args, **kwargs):
        super(FilterManager, self).__init__(*args, **kwargs)

        self._projectLabel = QtGui.QLabel(self)

        self._filtersList = filt = FiltersList(parent=self)
        self._matchersList = match = MatchersList(parent=self)
        self._actionsList = act = ActionsList(parent=self)

        self._toolbar = tb = QtGui.QToolBar(self)
        tb.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Minimum)
        tb.setMaximumHeight(32)
        tb.addAction(QtGui.QIcon(":/images/refresh.png"), "Refresh", self.refresh)

        self._status = QtGui.QStatusBar(self)

        h_splitter = QtGui.QSplitter(QtCore.Qt.Horizontal, self)
        h_splitter.setSizePolicy(QtGui.QSizePolicy.Preferred, QtGui.QSizePolicy.Expanding)

        v_splitter = QtGui.QSplitter(QtCore.Qt.Vertical, self)

        v_splitter.addWidget(self._matchersList)
        v_splitter.addWidget(self._actionsList)

        h_splitter.addWidget(self._filtersList)
        h_splitter.addWidget(v_splitter)

        tbLayout = QtGui.QHBoxLayout()
        tbLayout.setContentsMargins(0, 0, 4, 0)
        tbLayout.addWidget(self._toolbar)
        tbLayout.addWidget(self._projectLabel)

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(6, 0, 6, 4)
        layout.setSpacing(2)

        layout.addLayout(tbLayout)
        layout.addWidget(h_splitter)
        layout.addWidget(self._status)

        v_splitter.setSizes([100,100])
        h_splitter.setSizes([100,100])

        self.setStyleSheet("""
            DragDropItem {
                border: 1px solid black;
                border-radius: 4px;
                background-color: QLinearGradient(x1: 0, y1: 0, x2: 0, y2: 1, 
                                                  stop: 0 rgb(40, 40, 40), 
                                                  stop: 1 rgb(27, 28, 30) );
            }
            DragDropItem:checked {
                border: 1px solid rgb(100,100,100);
                background-color: QLinearGradient(x1: 0, y1: 0, x2: 0, y2: 1, 
                                                  stop: 1 rgb(40, 40, 40), 
                                                  stop: 0 rgb(27, 28, 30) );                
            }
            """)

        self.setProject(project)

        # Connnections
        filt.filterSelected.connect(match.setFilterObject)
        filt.filterSelected.connect(act.setFilterObject)


    def setProject(self, project):
        if project:
            self._filtersList.setProject(project)
            self._projectLabel.setText("Project: %s" % project.title)

    def refresh(self):
        self._filtersList.refresh()



########################
# DragDropList
#
class DragDropList(QtGui.QFrame):
    """
    A generic list widget that holds DragDropItem instances
    and supports drag and drop re-ordering.
    """
    ITEM_SPACING = 0
    COLUMN_WIDTHS = []

    def __init__(self, parent=None):
        super(DragDropList, self).__init__(parent)

        self.setFrameStyle(self.Panel|self.Sunken)
        self.setAcceptDrops(True)

        self._buttonGroup = QtGui.QButtonGroup(self)
        self._buttonGroup.setExclusive(True)

        header = QtGui.QWidget(self)
        header.setFixedHeight(20)
        headerLayout = QtGui.QHBoxLayout(header)
        headerLayout.setSpacing(0)
        headerLayout.setContentsMargins(8, 0, 8, 0)
        self._headerLayer = headerLayout

        self._contentWidget = content = QtGui.QWidget(self)
        scroll = QtGui.QScrollArea(self)
        scroll.setFrameStyle(self.Panel|self.Raised)
        scroll.setWidgetResizable(True)
        scroll.setWidget(content)
        scrollLayout = QtGui.QVBoxLayout(content)
        scrollLayout.setSpacing(0)
        scrollLayout.setContentsMargins(0, 0, 0, 0)

        self._itemLayout = QtGui.QVBoxLayout()
        self._itemLayout.setSpacing(self.ITEM_SPACING)
        self._itemLayout.setContentsMargins(0, 12, 0, 12)

        scrollLayout.addLayout(self._itemLayout)
        scrollLayout.addStretch()

        layout = QtGui.QVBoxLayout(self)
        layout.setContentsMargins(4, 4, 4, 4)

        layout.addWidget(header)
        layout.addWidget(scroll)

        # Connections
        self._buttonGroup.buttonClicked.connect(self.itemClicked)

    def __iter__(self):
        layout = self._itemLayout
        for i in xrange(layout.count()):
            yield layout.itemAt(i).widget()

    #########
    # Events
    #
    def mousePressEvent(self, event):
        super(DragDropList, self).mousePressEvent(event)
        self.setFocus()

    def dragEnterEvent(self, event):
        source = event.source()

        if isinstance(source, DragDropItem) and self.isAncestorOf(source):
            event.accept()
            return

        event.ignore()

    def dragMoveEvent(self, event):
        dropPos = event.pos()
        dropItem = self.childAt(dropPos)

        if dropItem and dropItem is event.source():
            event.ignore()
        else:
            event.accept()

    def dropEvent(self, event):
        layout = self._itemLayout
        parent = layout.parentWidget() or self

        sourceItem = event.source()
        sourceIndex = layout.indexOf(sourceItem)

        dropPos = event.pos()
        dropItem = parent.childAt(dropPos)

        insertIndex = None

        # The item was dropped directly on another item
        # so figure out if it was a bit above, or a bit below
        if dropItem:
            dropIndex = layout.indexOf(dropItem)
            middle = dropItem.geometry().center().y()
            if dropPos.y() < middle:
                # print "Drop source", sourceIndex, "before", dropIndex
                insertIndex = dropIndex
            else:
                # print "Drop source", sourceIndex, "after", dropIndex
                insertIndex = dropIndex 

        else:
            itemsRect = self.itemsRect()

            # The item was dropped somewhere inside the items layout
            if itemsRect.contains(dropPos):
                
                droppedY = dropPos.y()

                # Figure out which two items it was dropped between
                for child1, child2 in util.pairwise(self):

                    r1 = child1.geometry()
                    r2 = child2.geometry()
                    r1_bottom = r1.bottomLeft()
                    r2_top = r2.topRight()

                    testRect = QtCore.QRect(r1_bottom, r2_top)

                    if testRect.contains(dropPos):

                        if child1 is sourceItem or child2 is sourceItem:
                            # print "Dropped near original. Ignored."
                            event.ignore()
                            return

                        # print "Dropped between", child1, child2
                        insertIndex = layout.indexOf(child2)
                        break

            # This item was dropped above the first item
            # or below the last one
            else:
                if dropPos.y() < itemsRect.y():
                    # print "Dropped above layout"
                    insertIndex = 0
                else:
                    # print "Dropped below layout"
                    insertIndex = -1

        if insertIndex is not None:
            if insertIndex > sourceIndex:
                insertIndex -= 1
            # print "Final insert index is", insertIndex, "(from original %d)" % sourceIndex
            layout.insertWidget(insertIndex, sourceItem)
            event.acceptProposedAction()
        else:
            event.ignore()

    #
    #########

    def itemsRect(self):
        """
        Return a QRect of the specific boundary of the items in the layout
        """
        count = self._itemLayout.count()
        if not count:
            return QtCore.QRect(0,0,0,0)

        first = self._itemLayout.itemAt(0).widget()

        if count == 1:
            rect = first.geometry()
            return QtCore.QRect(rect.topLeft(), rect.bottomRight())

        last = self._itemLayout.itemAt(count-1).widget()
        return QtCore.QRect(first.geometry().topLeft(), last.geometry().bottomRight())

    def clear(self):
        """ Remove all items """
        layout = self._itemLayout 
        buttons = self._buttonGroup 

        while layout.count():
            item = layout.takeAt(0)
            widget = item.widget()
            if widget:
                buttons.removeButton(widget)
                widget.deleteLater()

    def appendItem(self, item):
        self._itemLayout.addWidget(item)
        self._buttonGroup.addButton(item)
        item.setColumnWidths(self.COLUMN_WIDTHS)

    def setHeaderLabels(self, labels):
        layout = self._headerLayer 
        while layout.count():
            item = layout.takeAt(0)
            widget = item.widget()
            if widget:
                widget.deleteLater()

        for i, name in enumerate(labels):
            label = QtGui.QLabel(name, self)
            try:
                label.setFixedWidth(self.COLUMN_WIDTHS[i])
            except:
                pass
            layout.addWidget(label)

    def refresh(self):
        pass

    def selectedFilter(self):
        return self._buttonGroup.checkedButton()


    def itemClicked(self, item):
        pass


########################
# DragDropItem
#
class DragDropItem(QtGui.QToolButton):
    """
    A custom widget to be used in a DragDropList
    """

    ITEM_SPACING = 6

    def __init__(self, *args, **kwargs):
        super(DragDropItem, self).__init__(*args, **kwargs)

        self.__dragStartPos = QtCore.QPoint(0,0)

        self.setMinimumHeight(24)
        self.setMaximumHeight(50)

        self.setCheckable(True)
        self.setFocusPolicy(QtCore.Qt.NoFocus)
        self.setSizePolicy(QtGui.QSizePolicy.Expanding, QtGui.QSizePolicy.Preferred)

        wrapperLayout = QtGui.QHBoxLayout(self)
        wrapperLayout.setSpacing(0)
        wrapperLayout.setContentsMargins(0,0,0,0)

        self._widgetLayout = layout = QtGui.QHBoxLayout()
        layout.setSpacing(self.ITEM_SPACING)
        layout.setContentsMargins(8,1,8,1)

        wrapperLayout.addLayout(layout)
        wrapperLayout.addStretch()

    def mousePressEvent(self, event):
        if event.button() == QtCore.Qt.LeftButton:
            self.__dragStartPos = event.pos()

        self.setChecked(True)
        super(DragDropItem, self).mousePressEvent(event)

    def mouseMoveEvent(self, event):
        startDrag = QtGui.QApplication.startDragDistance()

        if (event.pos() - self.__dragStartPos).manhattanLength() < startDrag:
            return

        mimeData = QtCore.QMimeData()
        data = cPickle.dumps(self.mapToParent(self.__dragStartPos))
        mimeData.setData("application/x-DragDropList", QtCore.QByteArray(data))

        pix = QtGui.QPixmap(self.size())
        self.render(pix)

        drag = QtGui.QDrag(self)
        drag.setMimeData(mimeData)
        drag.setPixmap(pix)
        drag.setHotSpot(event.pos())
        drag.exec_(QtCore.Qt.MoveAction)

    def setColumnWidths(self, widths):
        layout = self._widgetLayout
        for i, width in zip(xrange(layout.count()), widths):
            widget = layout.itemAt(i).widget()
            widget.setFixedWidth(max(width - self.ITEM_SPACING, 10))


########################
# FiltersList
#
class FiltersList(DragDropList):
    """
    Display a list of Filters for a project, 
    and allow them to be managed.
    """

    ITEM_SPACING = 10
    COLUMN_WIDTHS = [80]

    filterSelected = QtCore.Signal(object)

    def __init__(self, project=None, *args, **kwargs):
        super(FiltersList, self).__init__(*args, **kwargs)

        self.__project = None

        self.setHeaderLabels(['Enabled', 'Filter Name'])

        if project:
            self.setProject(project)

    def project(self):
        return self.__project 

    def setProject(self, project):
        if not isinstance(project, client.Project):
            raise TypeError("Invalid type %r. Must provide a Project instance" % type(project))

        self.__project = project
        self.refresh()

    def refresh(self):
        layout = self._itemLayout 
        layout.setEnabled(False)

        try:
            self.clear()

            if not self.__project:
                return 

            widths = self.COLUMN_WIDTHS

            filters = client.get_filters(self.__project)
            for f in filters:
                widget = FilterItem(f, self)
                self.appendItem(widget)

                widget.filterUpdated.connect(self._filterUpdated)
                widget.filterEnabled.connect(self._filterUpdated)

        finally:
            layout.setEnabled(True)

    def itemClicked(self, item):
        filterObj = item.filterObject()
        if filterObj:
            self.filterSelected.emit(filterObj)

    def _filterUpdated(self):
        print "refresh"
        self.refresh()


########################
# FilterItem
#
class FilterItem(DragDropItem):

    filterUpdated = QtCore.Signal()
    filterEnabled = QtCore.Signal(bool)

    def __init__(self, filterObj=None, *args, **kwargs):
        super(FilterItem, self).__init__(*args, **kwargs)

        self.__filter = None

        self._enabledCheck = check = QtGui.QCheckBox(self)
        check.setToolTip("Enable or Disable this filter")
        check.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)

        self._nameLabel = name = QtGui.QLineEdit(self)
        name.setPlaceholderText("<Set Filter Name>")
        name.setFrame(False)
        name.setReadOnly(True)
        name.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)
        name.installEventFilter(self)

        self._widgetLayout.addWidget(self._enabledCheck)
        self._widgetLayout.addWidget(self._nameLabel)

        self.setStyleSheet("""
            QCheckBox, QLineEdit {background-color: transparent; }
            QLineEdit {border: none; }
            """)

        if filterObj:
            self.setFilterObject(filterObj)

        # Connections
        name.editingFinished.connect(self.__nameEditingFinished)
        check.toggled.connect(self.__filterEnabled)

    def __repr__(self):
        f = self.__filter
        return "<FilterItem: %s >" % (f.name if f else "")

    def eventFilter(self, obj, event):
        if obj is self._nameLabel:

            typ = event.type()
            if typ == event.FocusIn:
                # we will trigger our own focus event
                self.click()
                return True

            elif typ == event.MouseButtonDblClick:
                obj.setReadOnly(False)
                obj.focusInEvent(QtGui.QFocusEvent(event.FocusIn, QtCore.Qt.MouseFocusReason))

        return super(FilterItem, self).eventFilter(obj, event)

    def filterObject(self):
        return self.__filter

    def setFilterObject(self, filt):
        self.__filter = filt
        self._enabledCheck.setChecked(filt.enabled)
        self._nameLabel.setText(filt.name)

    def setColumnWidths(self, widths):
        widgets = (self._enabledCheck, self._nameLabel)
        for width, widget in zip(widths, widgets):
            widget.setMinimumWidth(width)

    def __nameEditingFinished(self):
        nameLabel = self._nameLabel
        nameLabel.clearFocus()
        nameLabel.setReadOnly(True)

        filt = self.__filter

        newName = nameLabel.text()
        if not newName.strip():
            nameLabel.setText(filt.name)
            return

        if newName != filt.name:
            filt.set_name(newName)
            self.filterUpdated.emit()

    def __filterEnabled(self, enabled):
        if enabled != self.__filter.enabled:
            print "Toggle filter status", enabled
            # self.filterEnabled.emit(enabled)



########################
# MatchersList
#
class MatchersList(DragDropList):

    ITEM_SPACING = 6
    COLUMN_WIDTHS = [100, 120]

    def __init__(self, filterObj=None, parent=None):
        super(MatchersList, self).__init__(parent)

        self.__filter = None

        self.setHeaderLabels(['Matcher Field', 'Type', 'Value'])

        if filterObj:
            self.setFilterObject(filterObj)

    def filterObject(self):
        return self.__filter 

    def setFilterObject(self, filt):
        if not isinstance(filt, client.Filter):
            raise TypeError("Invalid type %r. Must provide a Filter instance" % type(filt))

        self.__filter = filt
        self.refresh()

    def refresh(self):
        layout = self._itemLayout 
        layout.setEnabled(False)

        try:
            self.clear()

            if not self.__filter:
                return 

            widths = self.COLUMN_WIDTHS

            matchers = self.__filter.get_matchers()

            for m in matchers:
                widget = MatcherItem(m, self)
                self.appendItem(widget)
                print "Matcher:", m.field, m.type, m.value

        finally:
            layout.setEnabled(True)


########################
# MatcherItem
#
class MatcherItem(DragDropItem):

    FIELDS = dict((f.replace('_', ' ').title(), getattr(client.MatcherField, f)) \
                    for f in dir(client.MatcherField) if not f.startswith('_'))

    FIELDS_SORTED = None

    TYPES = dict((t.replace('_', ' ').title(), getattr(client.MatcherType, t)) \
                    for t in dir(client.MatcherType) if not t.startswith('_'))

    TYPES_SORTED = None

    def __init__(self, matcherObj=None, *args, **kwargs):
        super(MatcherItem, self).__init__(*args, **kwargs)

        if self.FIELDS_SORTED is None:
            self.FIELDS_SORTED = sorted(self.FIELDS, key=lambda k: self.FIELDS[k])

        if self.TYPES_SORTED is None:
            self.TYPES_SORTED = sorted(self.TYPES, key=lambda k: self.TYPES[k])

        self.__matcher = None
        self.setMinimumHeight(30)
        self.setMaximumHeight(50)

        self._field = field = QtGui.QComboBox(self)
        field.addItems(self.FIELDS_SORTED)
        field.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)

        self._type = typ = QtGui.QComboBox(self)
        typ.addItems(self.TYPES_SORTED)
        typ.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)

        self._value = value = QtGui.QLineEdit(self)
        value.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)

        layout = self._widgetLayout
        layout.setContentsMargins(8, 4, 8, 4)
        layout.addWidget(field)
        layout.addWidget(typ)
        layout.addWidget(value)

        if matcherObj:
            self.setMatcherObject(matcherObj)

    def matcherObject(self):
        return self.__matcher

    def setMatcherObject(self, match):
        self.__matcher = match


########################
# ActionsList
#
class ActionsList(DragDropList):

    ITEM_SPACING = 6
    COLUMN_WIDTHS = [130]

    def __init__(self, filterObj=None, parent=None):
        super(ActionsList, self).__init__(parent)

        self.__filter = None

        self.setHeaderLabels(['Action Type', 'Value'])

        if filterObj:
            self.setFilterObject(filterObj)

    def filterObject(self):
        return self.__filter 

    def setFilterObject(self, filt):
        if not isinstance(filt, client.Filter):
            raise TypeError("Invalid type %r. Must provide a Filter instance" % type(filt))

        self.__filter = filt
        self.refresh()

    def refresh(self):
        layout = self._itemLayout 
        layout.setEnabled(False)

        try:
            self.clear()

            if not self.__filter:
                return 

            widths = self.COLUMN_WIDTHS

            actions = self.__filter.get_actions()

            for a in actions:
                widget = ActionItem(a, self)
                widget.setColumnWidths(widths)
                self.appendItem(widget)
                print "Action:", a.type, a.value

        finally:
            layout.setEnabled(True)


########################
# ActionItem
#
class ActionItem(DragDropItem):

    def __init__(self, actionObj=None, *args, **kwargs):
        super(ActionItem, self).__init__(*args, **kwargs)

        self._action = None

        # self._nameLabel = name = QtGui.QLineEdit(self)
        # name.setPlaceholderText("<Set Filter Name>")
        # name.setFrame(False)
        # name.setReadOnly(True)
        # name.setSizePolicy(QtGui.QSizePolicy.Minimum, QtGui.QSizePolicy.Preferred)
        # name.installEventFilter(self)

        # layout = QtGui.QHBoxLayout(self)
        # layout.setSpacing(0)
        # layout.setContentsMargins(8,1,8,1)
        # layout.addWidget(self._enabledCheck)
        # layout.addWidget(self._nameLabel)
        # layout.addStretch()

        self.setStyleSheet("""
            QLineEdit {border: none; }
            """)

        if actionObj:
            self.setActionObject(actionObj)

    def actionObject(self):
        return self._action

    def setActionObject(self, action):
        self._action = action




if __name__ == "__main__":
    from plow.gui.util import loadTheme 

    app = QtGui.QApplication([])
    loadTheme()

    proj = client.get_projects()[-1]

    f = FilterManager(proj)
    f.resize(800,600)
    f.show()
    f.raise_()

    app.exec_()