import conf

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

from rpc import RndServiceApi, RndProcessApi

def getPlowConnection():
    host, port = conf.PLOW_HOSTS[0]
    socket = TSocket.TSocket(host, int(port))
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RndServiceApi.Client(protocol)
    transport.open()
    return service

def getLocalConnection():
    socket = TSocket.TSocket("localhost", conf.NETWORK_PORT)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RndProcessApi.Client(protocol)
    transport.open()
    return service
