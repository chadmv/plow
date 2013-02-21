#!/usr/bin/env python

import logging
import sys

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
        core.ProcessMgr.runProcess(rtc)

    def killRunningTask(self, procId, reason):
        core.ProcessMgr.killRunningTask(procId, reason)

    def getRunningTasks(self):
        return core.ProcessMgr.getRunningTasks()

    def reboot(self, now=False):
        core.ProcessMgr.reboot(now)


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

    try:
        server.serve()
    except KeyboardInterrupt:
        sys.exit(2)
