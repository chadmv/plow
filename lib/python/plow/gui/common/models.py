
import re
import logging

from plow.gui.manifest import QtCore, QtGui

LOGGER = logging.getLogger(__name__)

DATA_ROLE = QtCore.Qt.UserRole 

_ALNUM_RX = re.compile('([0-9]+)')

def alphaNumericKey(aString):
    return [ int(c) if c.isdigit() else c for c in _ALNUM_RX.split(aString) ]


class AlnumSortProxyModel(QtGui.QSortFilterProxyModel):

    RX_ALNUMS = QtCore.QRegExp('(\d+|\D+)')

    def __init__(self, *args, **kwargs):
        super(AlnumSortProxyModel, self).__init__(*args, **kwargs)

        self.setSortRole(DATA_ROLE)
        self.__validAlnum = (str, unicode)

    def lessThan(self, left, right):
        sortRole = self.sortRole()
        leftData = left.data(sortRole)

        if isinstance(leftData, self.__validAlnum):
            
            rightData = right.data(sortRole)

            if leftData == rightData:
                return False

            return alphaNumericKey(leftData) < alphaNumericKey(rightData)

        return super(AlnumSortProxyModel, self).lessThan(left, right)


class PlowTableModel(QtCore.QAbstractTableModel):

    # A list of string headers for the model
    HEADERS = []

    # Map column number => callback that provides a string display val
    # for a given plow object
    DISPLAY_CALLBACKS = {}

    IdRole = QtCore.Qt.UserRole
    ObjectRole = QtCore.Qt.UserRole + 1
    DataRole = QtCore.Qt.UserRole + 2

    def __init__(self, parent=None):
        QtCore.QAbstractTableModel.__init__(self, parent)
        self._items = []
        self._index = {}

        self.__columnCount = len(self.HEADERS)

        # Should the refresh operation remove existing
        # items that are not found in each new update?
        self.refreshShouldRemove = True

    def fetchObjects(self):
        """
        Method that should be defined in subclasses, 
        to fetch new data that will be applied to the model. 

        Should return a list of objects
        """
        return []

    def hasChildren(self, parent):
        return False

    def refresh(self):
        updated = set()
        to_add = set()
        object_ids = set()

        rows = self._index
        columnCount = self.columnCount()
        parent = QtCore.QModelIndex()

        objects = self.fetchObjects()

        # Update existing
        for obj in objects:
            object_ids.add(obj.id)

            try:
                idx = self._index[obj.id]
                self._items[idx] = obj
                updated.add(obj.id)
                self.dataChanged.emit(self.index(idx,0), self.index(idx, columnCount-1))
            
            except (IndexError, KeyError):
                to_add.add(obj) 

        # Add new
        if to_add:
            size = len(to_add)
            start = len(self._items)
            end = start + size - 1
            self.beginInsertRows(parent, start, end)
            self._items.extend(to_add)
            self.endInsertRows()
            LOGGER.debug("adding %d new objects", size)

        # Remove missing
        if self.refreshShouldRemove:
            to_remove = set(self._index.iterkeys()).difference(object_ids)
            if to_remove:
                row_ids = ((rows[old_id], old_id) for old_id in to_remove)
                
                for row, old_id in sorted(row_ids, reverse=True):

                    self.beginRemoveRows(parent, row, row)
                    obj = self._items.pop(row)
                    self.endRemoveRows()

                    LOGGER.debug("removing %s %s", old_id, obj.name)

        # reindex the items
        self._index = dict(((item.id, i) for i, item in enumerate(self._items)))

    def rowCount(self, parent):
        if parent and parent.isValid():
            return 0
        return len(self._items)

    def columnCount(self, parent=None):
        if parent and parent.isValid():
            return 0
        return self.__columnCount

    def data(self, index, role):
        row = index.row()
        col = index.column()
        obj = self._items[row]

        if role == QtCore.Qt.DisplayRole:
            cbk = self.DISPLAY_CALLBACKS.get(col)
            if cbk is not None:
                return cbk(obj)
        
        elif role == QtCore.Qt.TextAlignmentRole:
            if col != 0:
                return QtCore.Qt.AlignCenter

        elif role == self.IdRole:
            return obj.id
       
        elif role == self.ObjectRole:
            return obj

        return None

    def setItemList(self, itemList):
        self.beginResetModel()
        self._items = itemList
        self._index = dict((n.id, row) for row, n in enumerate(itemList))
        self.endResetModel()

    def itemFromIndex(self, idx):
        if not idx.isValid():
            return None 

        item = self._items[idx.row()]
        return item

    def headerData(self, section, orientation, role):
        if role == QtCore.Qt.TextAlignmentRole:
            if section == 0:
                return QtCore.Qt.AlignLeft | QtCore.Qt.AlignVCenter 
            else:
                return QtCore.Qt.AlignCenter

        if role != QtCore.Qt.DisplayRole:
            return None 

        if orientation == QtCore.Qt.Vertical:
            return section 

        return self.HEADERS[section]