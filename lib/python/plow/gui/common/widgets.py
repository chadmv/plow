"""Non-Plow specific widgets."""
from plow.gui.manifest import QtGui, QtCore
from plow.gui.common.help import getHelp, getHelpTextWidget

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
    def __init__(self, filt=None, items=None, parent=None):
        QtGui.QWidget.__init__(self, parent)
        layout = QtGui.QVBoxLayout(self)

        self.text = QtGui.QLineEdit(self)
        self.items = QtGui.QListWidget(self)

        layout.addWidget(self.text)
        layout.addWidget(self.items)


class CheckableComboBox(QtGui.QWidget):
    """
    A combo box with selectable items.
    """
    def __init__(self, title, options, selected, icons=None, parent=None):
        QtGui.QWidget.__init__(self, parent)
        QtGui.QVBoxLayout(self)

        self.__btn = QtGui.QPushButton(title)
        self.__btn.setFocusPolicy(QtCore.Qt.NoFocus)
        self.__btn.setMaximumHeight(22)
        self.__btn.setFlat(True)
        self.__btn.setContentsMargins(0, 0, 0, 0)
        self.__menu = QtGui.QMenu(self)
        self.__btn.setMenu(self.__menu)

        for i, opt in enumerate(options):
            a = QtGui.QAction(self)
            a.setText(opt)
            a.setCheckable(True)
            if opt in selected:
                a.setChecked(True)
            if icons:
                try:
                    a.setIcon(icons[i])
                except IndexError:
                    pass
            self.__menu.addAction(a)

        self.layout().addWidget(self.__btn)

        self.__btn.toggled.connect(self.__btn.showMenu)

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
            if opt in checked:
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
        for item in items:
            list_item = self.__newItem(item)
            self.list_widget.addItem(list_item)
        self.list_widget.sortItems()

        self.btn_add = QtGui.QPushButton(QtGui.QIcon(":/plus.png"), "", self)
        self.btn_add.setFlat(True)
        self.btn_add.clicked.connect(self.addItem)
        self.btn_sub = QtGui.QPushButton(QtGui.QIcon(":/minus.png"), "", self)
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
        self.__btn.setIcon(QtGui.QIcon(":/help.png"))
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
    __C1 = QtGui.QColor(177, 24, 0)
    __C2 = QtGui.QColor(76, 115, 0)

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
        painter.setRenderHints(
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
