"""Non-Plow specific widgets."""
from plow.gui.manifest import QtGui, QtCore

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
    pass

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
