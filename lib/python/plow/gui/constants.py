
from manifest import QtGui

"""
The job status is not the same as the job state.  A job only has 2 states
the user can see: running and finished.

The job status is how the job is doin.

"""

BLUE = QtGui.QColor(28, 182, 214)
PURPLE = QtGui.QColor(214, 28, 205)
RED = QtGui.QColor(214, 48, 28)
ORANGE = QtGui.QColor(214, 94, 28)
GREEN = QtGui.QColor(39, 214, 28)
YELLOW = QtGui.QColor(214, 206, 28)
GRAY = QtGui.QColor(66, 66, 66)

COLOR_JOB_STATE = [
    GRAY,
    YELLOW.darker(),
    GREEN.darker()
]

COLOR_TASK_STATE = [
    GRAY,
    BLUE,
    YELLOW,
    RED,
    RED.darker(),
    PURPLE,
    GREEN
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
