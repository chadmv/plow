
from datetime import datetime

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

