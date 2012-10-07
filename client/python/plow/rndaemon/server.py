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

def start():
    logger.info("Staring Render Node Daemon on TCP port %d" % conf.NETWORK_PORT)
    handler = RndProcessHandler()
    processor = RndNodeApi.Processor(handler)
    transport = TSocket.TServerSocket(port=conf.NETWORK_PORT)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()
    server = TServer.TThreadedServer(processor, transport, tfactory, pfactory)
    server.serve()

