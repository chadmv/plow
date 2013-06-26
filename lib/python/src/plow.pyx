#cython: embedsignature=True

include "common.pxi"        
include "utils.pxi"
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
include "output.pxi"

#
# Module Init
#
plow_module_init()
PlowError = <object>_PlowError
PlowConnectionError = <object>_PlowConnectionError


@reconnecting
def get_plow_time():
    """
    Get the Plow server time in msec since the epoch 

    :returns: long - msec since epoch
    """
    cdef long epoch
    epoch = conn().proxy().getPlowTime()
    return long(epoch)
