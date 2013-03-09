
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
    date = datetime.fromtimestamp(epoch)
    return str(date)

def formatDuration(startTime, stopTime):

    if startTime == 0:
        return constants.TIME_NO_DURATION
    if stopTime == 0:
        stopTime = int(time.time())

    duration = stopTime - startTime
    sec = duration % 60
    minute = duration / 60
    hour = minute / 60
    if sec > 3600:
        minute = minute % minutue    

    return "%02d:%02d:%02d" % (hour, minute, sec)
