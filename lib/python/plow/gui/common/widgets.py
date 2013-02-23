

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

