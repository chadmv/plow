
from plow.gui.manifest import QtCore, QtGui


DATA_ROLE = QtCore.Qt.UserRole 


class AlnumSortProxyModel(QtGui.QSortFilterProxyModel):

    RX_ALNUMS = QtCore.QRegExp('(\d+|\D+)')

    def __init__(self, *args, **kwargs):
        super(AlnumSortProxyModel, self).__init__(*args, **kwargs)
        self.setSortRole(DATA_ROLE)

    def lessThan(self, left, right):
        sortRole = self.sortRole()
        leftData = left.data(sortRole)
        if isinstance(leftData, (str, unicode)):
            rightData = right.data(sortRole)
            return self.lessThanAlphaNumeric(leftData, rightData)

        return super(AlnumSortProxyModel, self).lessThan(left, right)

    def lessThanAlphaNumeric(self, left, right):
        if left == right:
            return False 

        alnums = self.RX_ALNUMS
        leftList = []
        rightList = []

        pos = 0
        while True:
            pos = alnums.indexIn(left, pos)
            if pos == -1:
                break

            leftList.append(alnums.cap(1))
            pos += alnums.matchedLength()

        pos = 0
        while True:
            pos = alnums.indexIn(right, pos)
            if pos == -1:
                break

            rightList.append(alnums.cap(1))
            pos += alnums.matchedLength()

        for leftItem, rightItem in zip(leftList, rightList):
            if leftItem != rightItem and leftItem.isdigit() and rightItem.isdigit():
                return int(leftItem) < int(rightItem)

            if leftItem != rightItem:
                return leftItem < rightItem

        return left < right