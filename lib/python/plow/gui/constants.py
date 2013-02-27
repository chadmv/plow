
from manifest import QtGui

"""
The job status is not the same as the job state.  A job only has 2 states
the user can see: running and finished.

The job status is how the job is doin.

"""
COLOR_JOB_STATUS_PAUSED = QtGui.QColor(85, 116, 213)
COLOR_JOB_STATUS_ERRORS = QtGui.QColor(97, 39, 39)

GRAY = QtGui.QColor(66, 66, 66)

COLOR_JOB_STATE = [
    GRAY,
    QtGui.QColor(53, 105, 24),
    QtGui.QColor(37, 207, 44)
]

COLOR_TASK_STATE = [
    GRAY,
    QtGui.QColor(109, 137, 213),
    QtGui.QColor(225, 225, 115),
    QtGui.QColor(97, 50, 51),
    QtGui.QColor(48, 25, 25),
    QtGui.QColor(201, 131, 255),
    QtGui.QColor(54, 106, 67)
]
