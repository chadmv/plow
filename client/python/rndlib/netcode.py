from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

from rpc import RndProcessApi, RndServiceApi

import conf

class RndProcessHandler(object):

    def __init__(self):
        pass


    def launch(self, process):
        print process


def start():
    handler = RndProcessHandler()
    processor = RndProcessApi.Processor(handler)
    transport = TSocket.TServerSocket(port=11338)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()
    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)
    server.serve()

def getPlowConnection():
    if conf.NETWORK_DISABLED:
        return None
    host, port = conf.PLOW_HOSTS[0]
    socket = TSocket.TSocket(host, int(port))
    transport = TTransport.TFramedTransport(self.__socket)
    protocol = TBinaryProtocol.TBinaryProtocol(self.__transport)
    service = RndServiceApi.Client(self.__protocol)
    transport.open()
    return service