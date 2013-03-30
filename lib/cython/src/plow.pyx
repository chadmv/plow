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

#
# Python imports
#
# from datetime import datetime
import uuid



def is_uuid(str identifier):
    cdef bint ret = False
    try:
        uuid.UUID(identifier)
        ret = True
    except ValueError:
        pass

    return ret 


def get_plow_time():
    cdef int epoch
    epoch = getClient().proxy().getPlowTime()
    # plowTime = datetime.fromtimestamp(epoch / 1000)
    # return plowTime
    return epoch




