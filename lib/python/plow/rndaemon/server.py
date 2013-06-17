#!/usr/bin/env python

import logging
import sys
import os
import signal

import conf
import core

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol.TBinaryProtocol import TBinaryProtocolAcceleratedFactory
from thrift.server import TServer

from rpc import RndNodeApi

logger = logging.getLogger(__name__)


class RndProcessHandler(object):

    def runTask(self, rtc):
        logger.debug("starting core.ProcessMgr.runProcess(rtc): %s", rtc.taskId)
        core.ProcessMgr.runProcess(rtc)
        logger.debug("finished core.ProcessMgr.runProcess(rtc): %s", rtc.taskId)

    def killRunningTask(self, procId, reason):
        core.ProcessMgr.killRunningTask(procId, reason)

    def getRunningTasks(self):
        logger.debug("starting core.ProcessMgr.getRunningTasks()")
        tasks = core.ProcessMgr.getRunningTasks()
        logger.debug("finished core.ProcessMgr.getRunningTasks()")
        return tasks

    def reboot(self, now=False):
        core.ProcessMgr.reboot(now)

    def pingPong(self, withTasks=False):
        ping = core.Profiler.getPing() 
        ping.isReboot = core.ProcessMgr.isReboot

        if withTasks:
            ping.tasks = self.getRunningTasks()

        return ping


def get_server(api, handler, port, **kwargs):
    processor = api.Processor(handler)
    socket = TSocket.TServerSocket(port=port)
    tfactory = kwargs.get('transport') or TTransport.TFramedTransportFactory()
    pfactory = kwargs.get('protocol') or TBinaryProtocolAcceleratedFactory()
    server = TServer.TThreadPoolServer(processor, socket, tfactory, pfactory)  
    server.setNumThreads(8)
    return server


def exit_handler(*args):
    logger.info("Caught SIGTERM. Shutting down Process Manager...")
    core.ProcessMgr.shutdown()
    logger.info("Process Manager finished shutting down")
    os._exit(0)

signal.signal(signal.SIGTERM, exit_handler)

def start():
    logger.info("Staring Render Node Daemon on TCP port %d" % conf.NETWORK_PORT)
    server = get_server(RndNodeApi, RndProcessHandler(), conf.NETWORK_PORT)

    try:
        server.serve()
    except KeyboardInterrupt:
        exit_handler()

    sys.exit(0)



