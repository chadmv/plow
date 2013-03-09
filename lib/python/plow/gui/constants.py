
from manifest import QtGui

"""
The job status is not the same as the job state.  A job only has 2 states
the user can see: running and finished.

The job status is how the job is doin.

"""

BLUE = QtGui.QColor(104, 161, 193)
PURPLE = QtGui.QColor(175, 38, 193)
RED = QtGui.QColor(193, 29, 4)
ORANGE = QtGui.QColor(193, 91, 2)
GREEN = QtGui.QColor(82, 193, 53)
YELLOW = QtGui.QColor(193, 185, 38)
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
