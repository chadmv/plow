#cython: embedsignature=True

cimport cython

from plow_types cimport *
from client cimport getClient, resetClient, PlowClient

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
# Module Init
#
plow_module_init()
PlowError = <object>_PlowError
PlowConnectionError = <object>_PlowConnectionError

#
# Python imports
#
import uuid
import time
import logging 


__HOST = "localhost"
__PORT = 11336

LOGGER = logging.getLogger("client")


cdef inline PlowClient* conn(bint reset=0) except *:
    cdef:
        PlowClient* c
        int i

    try:
        c = getClient(__HOST, __PORT, reset)
    except RuntimeError:
        try:
            c = getClient(__HOST, __PORT, True)
        except RuntimeError, e:
            raise PlowConnectionError(*e.args)

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
    resetClient()    


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


@cython.internal
cdef class PlowBase:

    def __cmp__(self, other):
        return cmp(hash(self), hash(other))
        
    def __hash__(self):
        return hash(self.id)

