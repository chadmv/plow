
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

ALPHA = 255
COLOR_TASK_STATE = [
    GRAY,
    QtGui.QColor(63, 79, 83),
    QtGui.QColor(83, 80, 63),
    QtGui.QColor(83, 80, 63),
    QtGui.QColor(83, 63, 74),
    QtGui.QColor(68, 63, 83),
    QtGui.QColor(68, 83, 63)
]

TIME_NO_TIME = "__-__ __:__:__"

TIME_NO_DURATION = "__:__:__"

TASK_STATES = ["INITIALIZE", 
               "WAITING",
               "RUNNING",
               "DEAD",
               "EATEN",
               "DEPEND",
               "SUCCEEDED"]
