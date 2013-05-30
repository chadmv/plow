
from manifest import QtGui

"""
The job status is not the same as the job state.  A job only has 2 states
the user can see: running and finished.

The job status is how the job is doin.

"""

#
# Colors
#
BLUE = QtGui.QColor(38, 98, 117)
PURPLE = QtGui.QColor(175, 38, 193)
RED = QtGui.QColor(152, 21, 0)
ORANGE = QtGui.QColor(243, 115, 33)
GREEN = QtGui.QColor(76, 115, 0)
YELLOW = QtGui.QColor(195, 174, 45)
GRAY = QtGui.QColor(66, 66, 66)

COLOR_JOB_STATE = [
    GRAY,
    YELLOW,
    GREEN,
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

#
# Display names
#
TIME_NO_TIME = "__-__ __:__:__"

TIME_NO_DURATION = "__:__:__"

TASK_STATES = ["INITIALIZE", 
               "WAITING",
               "RUNNING",
               "DEAD",
               "EATEN",
               "DEPEND",
               "SUCCEEDED"]

JOB_STATES = ["INITIALIZE", 
              "RUNNING",
              "FINISHED"]


#
# Sizes
#
DEFAULT_ROW_HEIGHT = 20