import random
import logging 

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

import conf
from rpc import RndServiceApi, RndNodeApi

logger = logging.getLogger(__name__)


def getPlowConnection(host=None, port=None):
    available = [h.split(":") for h in conf.PLOW_HOSTS]

    host_port = random.choice(available)
    conf_host, conf_port = host_port

    is_custom = host is not None or port is not None
    host = host if host is not None else conf_host
    port = port if port is not None else conf_port

    while True:
        socket = TSocket.TSocket(host, int(port))
        transport = TTransport.TFramedTransport(socket)

        try:
            transport.open()
        except TTransport.TTransportException, e:
            msg = "Failed to connect to Plow server %s:%s" % (host, port)
            if is_custom:
                logger.exception(msg)
            else:
                logger.warn(msg)
        else:
            break
        
        available.remove(host_port)
        if not available:
            raise IOError("No available Plow host. Connection attempts all failed")

        host_port = random.choice(available)
        host, port = host_port   

    logger.debug("Connected to Plow server %s:%s", host, port)
    protocol = TBinaryProtocol.TBinaryProtocolAccelerated(transport)
    service = RndServiceApi.Client(protocol)

    return (service, transport)


def getLocalConnection(port=None):
    if port is None:
        port = conf.NETWORK_PORT
    socket = TSocket.TSocket("localhost", port)
    transport = TTransport.TFramedTransport(socket)
    transport.open()
    protocol = TBinaryProtocol.TBinaryProtocolAccelerated(transport)
    service = RndNodeApi.Client(protocol)
    return (service, transport)

