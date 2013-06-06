"""Non-Plow specific widgets."""

from itertools import izip_longest

from plow.gui.manifest import QtGui, QtCore
from plow.gui.common.help import getHelp, getHelpTextWidget
from plow.gui import constants 


class TableWidget(QtGui.QTableView):
    def __init__(self, *args, **kwargs):
        super(TableWidget, self).__init__(*args, **kwargs)

        self.setEditTriggers(self.NoEditTriggers)
        self.setSelectionBehavior(self.SelectRows)
        self.setSelectionMode(self.ExtendedSelection)
        self.setSortingEnabled(True)
        self.setAlternatingRowColors(False)
        self.setAutoFillBackground(False)
        self.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        
        self.horizontalHeader().setStretchLastSection(True)
        
        vheader = self.verticalHeader()
        vheader.hide()
        vheader.setDefaultSectionSize(constants.DEFAULT_ROW_HEIGHT)        


class TreeWidget(QtGui.QTreeView):
    def __init__(self, *args, **kwargs):
        super(TreeWidget, self).__init__(*args, **kwargs)

        self.setSortingEnabled(True)
        self.setEditTriggers(self.NoEditTriggers)
        self.setSelectionBehavior(self.SelectRows)
        self.setSelectionMode(self.ExtendedSelection)
        self.setUniformRowHeights(True)
        self.setAlternatingRowColors(False)
        self.setAutoFillBackground(True)
        self.viewport().setFocusPolicy(QtCore.Qt.NoFocus)
        # self.setVerticalScrollMode(self.ScrollPerPixel)


class SpinSliderWidget(QtGui.QWidget):
    def __init__(self, minimum, maximum, value, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QHBoxLayout(self)
        layout.setContentsMargins(0, 0, 0, 0)

        self.slider = QtGui.QSlider(QtCore.Qt.Horizontal, self)
        self.slider.setMaximum(maximum)
        self.slider.setMinimum(minimum)
        self.slider.setValue(value)

        self.spin = QtGui.QSpinBox(self)
        self.spin.setRange(minimum, maximum)
        self.spin.setValue(value)

        self.spin.valueChanged.connect(self.slider.setValue)
        self.slider.valueChanged.connect(self.spin.setValue)

        layout.addWidget(self.slider)
        layout.addWidget(self.spin)

    def value(self):
        return self.slider.value()


class BooleanCheckBox(QtGui.QCheckBox):
    def __init__(self, checked=True, parent=None):
        QtGui.QCheckBox.__init__(self, parent)
        self.setChecked(checked)

    def setChecked(self, value):
        if value:
            self.setCheckState(QtCore.Qt.Checked)
        else:
            self.setCheckState(QtCore.Qt.Unchecked)

    def isChecked(self):
        return self.checkState() == QtCore.Qt.Checked


class FilterableListBox(QtGui.QWidget):
    """
    A list box widget with a text filter.
    """

    DATA_ROLE = QtCore.Qt.UserRole

    selectionChanged = QtCore.Signal(list)
    valueDoubleClicked = QtCore.Signal(object)
    valueClicked = QtCore.Signal(object)

    def __init__(self, filt=None, items=None, data=None, parent=None):
        QtGui.QWidget.__init__(self, parent)

        self.__data = {}

        self.__txt_label = QtGui.QLabel(self)
        self.__txt_filter = QtGui.QLineEdit(self)
        self.__txt_filter.textChanged.connect(self.__filterChanged)

        self.__model = QtGui.QStringListModel(self)

        self.__proxyModel = proxy = QtGui.QSortFilterProxyModel(self)
        proxy.setSourceModel(self.__model)

        self.__list = view = QtGui.QListView(self)
        view.setSelectionMode(self.__list.ExtendedSelection)
        view.setModel(proxy)

        proxy.sort(0)
        proxy.setDynamicSortFilter(True)

        layout = QtGui.QVBoxLayout(self)
        layout.setSpacing(4)
        layout.setContentsMargins(0, 0, 0, 0)

        hlayout = QtGui.QHBoxLayout()
        hlayout.setContentsMargins(0, 0, 0, 0)
        hlayout.addWidget(self.__txt_label)
        hlayout.addWidget(self.__txt_filter)

        layout.addLayout(hlayout)
        layout.addWidget(self.__list)

        # connections
        self.__list.doubleClicked.connect(self._itemDoubleClicked)
        self.__list.clicked.connect(self._itemClicked)
        self.__list.selectionModel().selectionChanged.connect(self._selectionChanged)

        if items:
            self.setStringList(items)

        if filt:
            self.setFilter(filt)

    def clear(self):
        self.setStringList([])
        self.setFilter('')

    def clearSelection(self, clearFilter=True):
        self.__list.clearSelection()
        if clearFilter:
            self.setFilter('')

    def setLabel(self, val):
        self.__txt_label.setText(val)

    def setFilter(self, val, selectFirst=False):
        if not val:
            val = ''

        self.__txt_filter.setText(val)

        if not selectFirst:
            return 

        proxy = self.__proxyModel
        matches = proxy.match(proxy.index(0,0), QtCore.Qt.DisplayRole, val, 1, QtCore.Qt.MatchContains)
        if matches:
            selModel = self.__list.selectionModel()
            selModel.select(matches[0], selModel.ClearAndSelect)

    def setStringList(self, aList, data=None):
        model = self.__model
        model.setStringList(aList)
        self.__data = {}

        role = self.DATA_ROLE
        for row, val in enumerate(aList):
            try:
                dataVal = data[row]
            except Exception, e:
                dataVal = val
            self.__data[row] = dataVal

    def setSingleSelections(self, enabled):
        if enabled:
            mode = self.__list.SingleSelection
        else:
            mode = self.__list.ExtendedSelection
        self.__list.setSelectionMode(mode)

    def getSelectedValues(self, role=QtCore.Qt.DisplayRole):
        indexes = self.__list.selectedIndexes()

        if self.__list.selectionMode() == self.__list.SingleSelection:
            indexes = indexes[:1]

        proxy = self.__proxyModel
        sourceModel = proxy.sourceModel()
        data = self.__data 

        if role == self.DATA_ROLE:
            values = [data.get(proxy.mapToSource(i).row()) for i in indexes]
        else:
            values = [proxy.data(i) for i in indexes]

        return values

    def __filterChanged(self, value):
        value = value.strip()
        if not value:
            self.__proxyModel.setFilterFixedString("")

        else:
            searchStr = '*'.join(value.split())
            self.__proxyModel.setFilterWildcard(searchStr)

    def _itemDoubleClicked(self, item):
        data = self.__proxyModel.data(item)
        self.valueDoubleClicked.emit(data)

    def _itemClicked(self, item):
        data = self.__proxyModel.data(item)
        self.valueClicked.emit(data)

    def _selectionChanged(self):
        sel = self.getSelectedValues()
        self.selectionChanged.emit(sel)


class CheckableComboBox(QtGui.QWidget):
    """
    A combo box with selectable items.
    """
    
    optionSelected = QtCore.Signal(str)

    def __init__(self, title, options, selected=None, icons=None, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)

        self.__btn = btn = QtGui.QPushButton(title)
        btn.setFocusPolicy(QtCore.Qt.NoFocus)
        btn.setMaximumHeight(22)
        btn.setFlat(True)
        btn.setContentsMargins(0, 0, 0, 0)

        self.__menu = menu = QtGui.QMenu(self)
        btn.setMenu(menu)

        self.setOptions(options, selected, icons)

        layout.addWidget(btn)

        btn.toggled.connect(btn.showMenu)
        menu.triggered.connect(lambda action: self.optionSelected.emit(action.text()))

    def options(self):
        return [a.text() for a in self.__menu.actions()]

    def setOptions(self, options, selected=None, icons=None):
        if selected and not isinstance(selected, (set, dict)):
            selected = set(selected)

        menu = self.__menu
        menu.clear()

        for opt, icon in izip_longest(options, icons or []):
            a = QtGui.QAction(menu)
            a.setText(opt)
            a.setCheckable(True)
            if selected and opt in selected:
                a.setChecked(True)
            if icon:
                a.setIcon(icons[i])
            menu.addAction(a)        

    def selectedOptions(self):
        return [a.text() for a in self.__menu.actions() if a.isChecked()]


class CheckableListBox(QtGui.QWidget):
    """
    A list box with selectable items.
    """
    def __init__(self, title, options, checked, allSelected=True, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)

        self.listItems = QtGui.QListWidget(self)
        self.listItems.setMaximumHeight(100)

        for opt in options:
            item = QtGui.QListWidgetItem(opt)
            item.setFlags(QtCore.Qt.ItemIsUserCheckable | QtCore.Qt.ItemIsSelectable | QtCore.Qt.ItemIsEnabled)
            if checked and opt in checked:
                item.setCheckState(QtCore.Qt.Checked)
            else:
                item.setCheckState(QtCore.Qt.Unchecked)
            self.listItems.addItem(item)

        self.checkBoxAll = QtGui.QCheckBox("All %s" % title, self)
        if allSelected:
            self.listItems.setDisabled(True)
            self.checkBoxAll.setCheckState(QtCore.Qt.Checked)
        self.checkBoxAll.stateChanged.connect(self.__allSelectedToggled)

        layout.addWidget(self.checkBoxAll)
        layout.addWidget(self.listItems)

    def isAllSelected(self):
        return self.checkBoxAll.checkState() == QtCore.Qt.Checked

    def getCheckedOptions(self):
        result = []
        if self.isAllSelected():
            return result
        for i in xrange(0, self.listItems.count()):
            item = self.listItems.item(i)
            if item.checkState() == QtCore.Qt.Checked:
                result.append(str(item.text()))
        return result

    def __allSelectedToggled(self, state):
        if state == QtCore.Qt.Checked:
            self.listItems.setDisabled(True)
        else:
            self.listItems.setDisabled(False)


class RadioBoxArray(QtGui.QWidget):
    """
    An array of linked radio boxes.
    """
    def __init__(self, title, options, cols=3, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)

        group_box = QtGui.QGroupBox(title)
        group_box_layout = QtGui.QGridLayout(group_box)

        row = 0
        for item, opt in enumerate(options):
            row = item / cols
            radio = QtGui.QRadioButton(opt, self)
            group_box_layout.addWidget(radio, row, item % cols)

        layout.addWidget(group_box)


class ManagedListWidget(QtGui.QWidget):
    """
    A list widget that lets you add/remove things.
    """
    def __init__(self, items, default="name", parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)
        self.__default = default

        self.setMaximumHeight(200)

        self.list_widget = QtGui.QListWidget(self)
        self.list_widget.itemDoubleClicked.connect(self.list_widget.editItem)
        for item in (items or []):
            list_item = self.__newItem(item)
            self.list_widget.addItem(list_item)
        self.list_widget.sortItems()

        self.btn_add = QtGui.QPushButton(QtGui.QIcon(":/images/plus.png"), "", self)
        self.btn_add.setFlat(True)
        self.btn_add.clicked.connect(self.addItem)
        self.btn_sub = QtGui.QPushButton(QtGui.QIcon(":/images/minus.png"), "", self)
        self.btn_sub.setFlat(True)
        self.btn_sub.clicked.connect(self.removeItems)

        layout_btn = QtGui.QHBoxLayout()
        layout_btn.setContentsMargins(0, 0, 0, 0)
        layout_btn.setSpacing(1)
        layout_btn.addStretch()
        layout_btn.addWidget(self.btn_add)
        layout_btn.addWidget(self.btn_sub)

        self.layout().addWidget(self.list_widget)
        self.layout().addLayout(layout_btn)

    def getValues(self):
        result = []
        for i in range(0, self.list_widget.count()):
            result.append(str(self.list_widget.item(i).text()))
        return result

    def addItem(self):
        item = self.__newItem(self.__default)
        self.list_widget.addItem(item)
        self.list_widget.editItem(item)

    def removeItems(self):
        for item in self.list_widget.selectedItems():
            self.list_widget.takeItem(self.list_widget.row(item))

    def __newItem(self, name):
        list_item = QtGui.QListWidgetItem(name)
        list_item.setFlags(
                QtCore.Qt.ItemIsEditable | 
                QtCore.Qt.ItemIsSelectable |
                QtCore.Qt.ItemIsEnabled)
        return list_item


class FormWidgetLabel(QtGui.QWidget):
    def __init__(self, text, help, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QHBoxLayout(self)
        self.__help = help

        self.__btn = QtGui.QToolButton(self)
        self.__btn.setIcon(QtGui.QIcon(":/images/help.png"))
        self.__btn.setFocusPolicy(QtCore.Qt.NoFocus)
        self.__btn.clicked.connect(self.__show_popup)
        self.__btn.setStyleSheet("QToolButton { border: 0px }")
 
        self.__label = QtGui.QLabel(text, self)

        self.layout().setContentsMargins(0, 0, 0, 0)
        self.layout().setSpacing(0)

        self.layout().addWidget(self.__btn)
        self.layout().addSpacing(5)
        self.layout().addWidget(self.__label)
        self.layout().addStretch()

    def __show_popup(self):

        frame = QtGui.QFrame(self, QtCore.Qt.Popup | QtCore.Qt.Window)
        frame.resize(350, 200)
        frame.setFrameStyle(QtGui.QFrame.Box | QtGui.QFrame.Raised)
        frame.setLineWidth(2);
        frame.move(QtGui.QCursor.pos())
        
        layout = QtGui.QVBoxLayout(frame)
        layout.addWidget(getHelpTextWidget(self.__help))
        frame.show()


class SimplePercentageBarDelegate(QtGui.QStyledItemDelegate):
    """
    A simple status bar, much like a heath meter, which 
    is intended to show the ratio of two values.
    """
    # Left, top, right, bottom
    Margins = [5, 4, 5, 4]

    __PEN = QtGui.QColor(33, 33, 33)
    __C1 = constants.RED
    __C2 = constants.GREEN

    def __init__(self, parent=None):
        QtGui.QStyledItemDelegate.__init__(self, parent)

    def paint(self, painter, option, index):

        if not index.isValid():
            QtGui.QStyledItemDelegate.paint(self, painter, option, index)
            return

        ## Broken in PySide.
        opt = QtGui.QStyleOptionViewItemV4(option)
        self.initStyleOption(opt, index);

        # Broken in pyside 1.1.2
        #if opt.state & QtGui.QStyle.State_Selected:
        #    painter.fillRect(opt.rect, opt.palette.highlight())

        rect = opt.rect
        rect.adjust(self.Margins[0], self.Margins[1], -self.Margins[2], -self.Margins[3])
        data = index.data()

        painter.save()
        painter.setRenderHints (
            painter.HighQualityAntialiasing |
            painter.SmoothPixmapTransform |
            painter.Antialiasing)

        painter.setPen(self.__PEN)

        if data[1] == 0:
            painter.setBrush(self.__C1)
            painter.drawRoundedRect(rect, 3, 3)
        else:
            ratio = data[0] / float(data[1])
            painter.setBrush(self.__C1)
            painter.drawRoundedRect(rect, 3, 3)
            rect.setWidth(ratio * rect.width())
            painter.setBrush(self.__C2)
            painter.drawRoundedRect(rect, 3, 3)
        painter.restore()


class ResourceDelegate(QtGui.QItemDelegate):
    """
    A custom QItemDelegate to be set onto a specific
    column of a view, containing numeric data that can
    be represented as a resource. 

    The default role to check for this data on an index 
    is Qt.UserRole, but can be set to any other role to 
    source the numeric data. 

    Example:
        If we have a column in our view that contains a 
        percentage value of how much memory is left in the 
        system (from 0.0 - 1.0), then we can do:

           delegate = ResourceDelegate(warn=.5, critical=.1)

        This will show a warning indication when the ratio is 
        below 50%, and a critical indication when it falls below 
        10%

        If we are storing our data in another role... 

            otherRole = QtCore.Qt.UserRole + 50
            delegate = ResourceDelegate(dataRole=otherRole)

    """
    COLOR_CRITICAL = constants.RED
    COLOR_WARN = constants.YELLOW
    COLOR_OK = constants.GREEN
    COLOR_BG = constants.GRAY

    def __init__(self, warn=0.15, critical=0.05, dataRole=QtCore.Qt.UserRole, parent=None):
        super(ResourceDelegate, self).__init__(parent)
        self._warn = warn 
        self._crit = critical
        self._role = dataRole

    def paint(self, painter, opts, index):
        currentData = index.data(self._role)
        try:
            ratio = float(currentData)
        except:
            super(ResourceDelegate, self).paint(painter, opts, index)
            return 

        text = "%0.2f%%" % (ratio * 100)
        opt = QtGui.QStyleOptionViewItemV4(opts)
        opt.displayAlignment = QtCore.Qt.AlignRight|QtCore.Qt.AlignVCenter

        grad = QtGui.QLinearGradient(opt.rect.topLeft(), opt.rect.topRight())
        # darkEnd = QtCore.Qt.transparent
        darkEnd = self.COLOR_BG
        end = darkEnd 

        if ratio == 1:
            darkEnd = self.COLOR_OK
            end = darkEnd

        elif ratio <= self._crit:
            darkEnd = self.COLOR_CRITICAL
            end = self.COLOR_CRITICAL

        elif ratio <= self._warn:
            darkEnd = self.COLOR_WARN
            end = self.COLOR_WARN

        grad.setColorAt(0.0, self.COLOR_OK)
        grad.setColorAt(min(ratio, 1.0), self.COLOR_OK)
        grad.setColorAt(min(ratio + .01, 1.0), end)
        grad.setColorAt(1.0, darkEnd)

        self.drawBackground(painter, opt, index)
        painter.fillRect(opt.rect, QtGui.QBrush(grad))
        self.drawDisplay(painter, opt, opt.rect, text)

        state_bg = index.data(QtCore.Qt.BackgroundRole)
        if state_bg:
            painter.setBrush(QtCore.Qt.NoBrush)
            pen = QtGui.QPen(state_bg)
            pen.setWidth(2)
            painter.setPen(pen)
            painter.drawRect(opt.rect)