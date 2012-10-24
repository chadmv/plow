import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '../../../'))

import tempfile
import unittest
import time
import threading

import plow.rndaemon.conf as conf 
conf.NETWORK_DISABLED = True

import plow.rndaemon.rpc.ttypes as ttypes
import plow.rndaemon.core as core

import logging
logging.basicConfig(level=logging.DEBUG)


class TestResourceManager(unittest.TestCase):

    def testCoreCheckout(self):
        manager = core.ResourceMgr
        totalCores = core.Profiler.physicalCpus

        assert totalCores == len(manager.getSlots())
        assert totalCores == len(manager.getOpenSlots())

        slots = manager.checkout(1)
        assert len(manager.getOpenSlots()) == (totalCores-1)

        slots += manager.checkout(1)
        assert len(manager.getOpenSlots()) == (totalCores-2)

        manager.checkin(slots)
        assert len(manager.getOpenSlots()) == totalCores


class TestProcessManager(unittest.TestCase):

    def setUp(self):
        self._logdir = tempfile.gettempdir()
        self._logfile = os.path.join(self._logdir, "rndlogfile.log")

    def testRunTaskCommand(self):
        process = ttypes.RunTaskCommand()
        process.procId = "a"
        process.taskId=  "b"
        process.cores = 1
        process.command=["/bin/ls", self._logdir]
        process.env = { },
        process.logFile = self._logfile 
        core.runProcess(process)
        time.sleep(1)

    def testRunTaskCommandOutOfCores(self):
        process = ttypes.RunTaskCommand()
        process.procId = "a"
        process.taskId =  "b"
        process.cores = 9999
        process.command=["/bin/ls", self._logdir]
        process.env = { },
        process.logFile = self._logfile 
        self.assertRaises(ttypes.RndException, core.runProcess, process)


class TestCommunications(unittest.TestCase):
    """
    Creates a mock server to accept communication tests 
    from the client API 
    """

    def setUp(self):
        from plow.rndaemon.server import get_server
        from plow.rndaemon.rpc import RndServiceApi 

        self.event = threading.Event()

        self.server_port = 9090

        handler = ServiceHandler(self.event)
        self.server = get_server(RndServiceApi, handler, self.server_port)

        self.t_server = threading.Thread(target=self.server.serve)
        self.t_server.daemon = True
        self.t_server.start()


    def tearDown(self):
        self.server.serverTransport.close()


    def testSendPing(self):
        """
        Get a connection to the local "server" and test 
        that it receives a ping.
        """
        from plow.rndaemon.client import getPlowConnection
        from plow.rndaemon.rpc.ttypes import Ping, Hardware

        ping = Ping()
        ping.hw = Hardware()

        service, transport = getPlowConnection('localhost', self.server_port)
        service.sendPing(ping)
        self.event.wait(3)

        self.assertTrue(
            self.event.is_set(), 
            msg="Server did not receive ping from client in reasonable time")

        transport.close()


class ServiceHandler(object):

    def __init__(self, evt):
        self.event = evt 

    def sendPing(self, ping):
        self.event.set()



if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (TestResourceManager, TestProcessManager, TestCommunications):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)


