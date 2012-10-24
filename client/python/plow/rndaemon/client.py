import conf

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

from rpc import RndServiceApi, RndNodeApi

def getPlowConnection(host=None, port=None):
    conf_host, conf_port = conf.PLOW_HOSTS[0].split(":")
    if host is None:
        host = conf_host
    if port is None:
        port = conf_port
    socket = TSocket.TSocket(host, int(port))
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RndServiceApi.Client(protocol)
    transport.open()
    return (service, transport)

def getLocalConnection():
    socket = TSocket.TSocket("localhost", conf.NETWORK_PORT)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RndNodeApi.Client(protocol)
    transport.open()
    return service
