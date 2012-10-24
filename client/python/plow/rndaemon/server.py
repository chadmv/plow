import logging

import conf
import core

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

from rpc import RndNodeApi

logger = logging.getLogger(__name__)

class RndProcessHandler(object):

    def runTask(self, rtc):
        core.runProcess(rtc)


def get_server(api, handler, port):
    processor = api.Processor(handler)
    socket = TSocket.TServerSocket(port=port)
    tfactory = TTransport.TFramedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()
    server = TServer.TThreadedServer(processor, socket, tfactory, pfactory)    
    return server


def start():
    logger.info("Staring Render Node Daemon on TCP port %d" % conf.NETWORK_PORT)
    server = get_server(RndNodeApi, RndProcessHandler(), conf.NETWORK_PORT)
    server.serve()