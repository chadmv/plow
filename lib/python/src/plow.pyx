cimport cython

from plow_types cimport *
from client cimport getClient, PlowClient

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

#
# Python imports
#

# from datetime import datetime
import uuid
import time
import logging 


__HOST = "localhost"
__PORT = 11336

LOGGER = logging.getLogger("client")

EX_CONNECTION = set([
    "write() send(): Broken pipe",
    "Called write on non-open socket",
    "No more data to read.",
    "connect() failed: Connection refused"
    ])




cdef inline PlowClient* conn(bint reset=0) except *:
    cdef:
        PlowClient* c
        int i

    try:
        c = getClient(__HOST, __PORT, reset)
    except RuntimeError:
        c = getClient(__HOST, __PORT, True)

    return c


def reconnect():
    """
    Re-establish the connection to the Plow server
    """
    cdef PlowClient* c = conn(True)
    c.reconnect()


def set_host(str host="localhost", int port=11336):
    """
    Set the host and port of the Plow server

    :param host: str = "localhost"
    :param port: int = 11336
    """
    global __HOST, __PORT
    __HOST = host
    __PORT = port
    reconnect()    


def get_host():
    """
    Get the current host and port of the Plow server

    :returns: (str host, int port)
    """
    return __HOST, __PORT


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


@reconnecting
def get_plow_time():
    """
    Get the Plow server time in msec since the epoch 

    :returns: long - msec since epoch
    """
    cdef long epoch
    epoch = conn().proxy().getPlowTime()
    return long(epoch)




