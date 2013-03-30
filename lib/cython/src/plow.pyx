cimport cython
from plow_types cimport *
from client cimport getClient

include "project.pxi"
include "folder.pxi"
include "job.pxi"
include "layer.pxi"
include "task.pxi"
include "node.pxi"
include "cluster.pxi"
include "filter.pxi"
include "quota.pxi"
include "depend.pxi"

# from datetime import datetime


def get_plow_time():
    cdef int epoch
    epoch = getClient().proxy().getPlowTime()
    # plowTime = datetime.fromtimestamp(epoch / 1000)
    # return plowTime
    return epoch




