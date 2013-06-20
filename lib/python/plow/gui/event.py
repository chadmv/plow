
from manifest import QtCore


class _EventManager(QtCore.QObject):

    GlobalRefresh = QtCore.Signal()

    ProjectOfInterest = QtCore.Signal(str)
    FolderOfInterest = QtCore.Signal(str)
    JobOfInterest = QtCore.Signal(str)
    LayerOfInterest = QtCore.Signal(str)
    TaskOfInterest = QtCore.Signal(str, str) # taskId, optional jobId

    ClusterOfInterest = QtCore.Signal(str)
    NodeOfInterest = QtCore.Signal(str)


EventManager = _EventManager()
