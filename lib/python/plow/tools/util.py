
import os
import time
from datetime import datetime

import constants

def formatPercentage(value, total):
    if total == 0:
        return "0.00%"
    else:
        return "%0.2f%%" % ((value / float(total)) * 100)

def formatMaxValue(value):
    if value == -1:
        return "-"
    else:
        return "%02d" % value

def formatDateTime(epoch):
    if not epoch:
        return "-"

    date = datetime.fromtimestamp(epoch / 1000).strftime("%Y-%m-%d %H:%M:%S")

    return str(date)

def formatDuration(startTime, stopTime=0):
    """
    Format a duration to hh:mm::ss
    """
    if startTime == 0:
        return constants.TIME_NO_DURATION

    if stopTime == 0:
        stopTime = int(time.time()) * 1000

    duration = max((stopTime - startTime) / 1000, 0)
    m, s = divmod(duration, 60)
    h, m = divmod(m, 60)

    return "%02d:%02d:%02d" % (h, m, s)

def clipText(text, size):
    """
    Returns text clipped to a particular size.
    """
    if len(text) >= size:
        return text[0:size-2] + "..."
    else:
        return text

def formatCoreTime(core_time_ms):
    """
    Format core time in millis to core hours.
    """
    return "%0.2f" % (core_time_ms / 1000 / 3600.0)