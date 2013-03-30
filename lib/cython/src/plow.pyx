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
    """
    Test if a string is a valid UUID 

    :param identifier: string to test 
    :type identifier: str
    :returns: bool - True if valid UUID
    """
    cdef bint ret = False
    try:
        uuid.UUID(identifier)
        ret = True
    except ValueError:
        pass

    return ret 


def get_plow_time():
    """
    Get the Plow server time in msec since the epoch 

    :returns: long - msec since epoch
    """
    cdef long epoch
    epoch = getClient().proxy().getPlowTime()
    # plowTime = datetime.fromtimestamp(epoch / 1000)
    # return plowTime
    return epoch




