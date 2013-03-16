
import constants
import time

from datetime import datetime

def formatPercentage(value, total):
    if total == 0:
        return "0.00%"
    else:
        return "%0.2f%%" % (value / float(total))

def formatMaxValue(value):
    if value == -1:
        return "-"
    else:
        return "%02d" % value

def formatDateTime(epoch):
    if not epoch:
        return "-"
    date = datetime.fromtimestamp(epoch / 1000)
    return str(date)

def formatDuration(startTime, stopTime):
    """
    Format a duration to hh:mm::ss
    """
    if startTime == 0:
        return constants.TIME_NO_DURATION
    if stopTime == 0:
        stopTime = int(time.time()) * 1000
    duration = (stopTime - startTime) / 1000
    m, s = divmod(duration, 60)
    h, m = divmod(m, 60)
    return "%02d:%02d:%02d" % (h, m, s)
