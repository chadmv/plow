cimport cython

from plow_types cimport *
from client cimport getClient, resetClient, PlowClient

#
# Python imports
#
import uuid
import time
import logging 
import random

__HOST = None
__PORT = None

PLOW_HOSTS = [("localhost", 11336)]

LOGGER = logging.getLogger("client")


@cython.internal
cdef class PlowBase:

    def __cmp__(self, other):
        return cmp(hash(self), hash(other))
        
    def __hash__(self):
        return hash(self.id)



cdef PlowClient* conn(bint reset=0) except *:
    cdef:
        PlowClient* c
        string host
        int port
        tuple host_port
        list available
        bint is_custom = False
        bint first_loop = True

    global __HOST, __PORT

    if __HOST is None or __PORT is None:
        host_port = random.choice(PLOW_HOSTS)
        host, port = host_port
    else:
        is_custom = True
        host, port = __HOST, __PORT

    while True:

        try:
            c = getClient(host, port, reset)
        except RuntimeError:
            try:
                c = getClient(host, port, True)
            except RuntimeError, e:
                msg = "Failed to connect to Plow server %s:%s" % (host, port)
                LOGGER.warn(msg)
                if is_custom:
                    raise PlowConnectionError(*e.args)
            else:
                break
        else:
            break
        
        if first_loop:
            available = PLOW_HOSTS[:]
            first_loop = False

        available.remove(host_port)
        if not available:
            raise PlowConnectionError("No available Plow host. Connection attempts all failed", 0)

        host_port = random.choice(available)
        host, port = host_port

    LOGGER.debug("Connected to %s:%d", host, port)
    __HOST, __PORT = host, port

    return c


def reconnect():
    """
    Re-establish the connection to the Plow server
    """
    global __HOST, __PORT
    __HOST = None
    __PORT = None

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




