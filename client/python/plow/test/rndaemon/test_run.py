#!/usr/bin/env python

import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '../../../'))

import tempfile
import unittest
import time
from multiprocessing import Process, Event 

import plow.conf as conf 
import plow.rndaemon.conf as rndconf 
rndconf.NETWORK_DISABLED = True

import plow.rndaemon.rpc.ttypes as ttypes
import plow.rndaemon.core as core

import logging
logging.basicConfig(level=logging.DEBUG)

PLOW_ROOT = conf.Config.get('env', 'plow_root')


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
        process = self.getNewTaskCommand()
        process.command = ["/bin/ls", self._logdir]
        core.ProcessMgr.runProcess(process)
        time.sleep(1)

    def testRunTaskCommandOutOfCores(self):
        process = self.getNewTaskCommand()
        process.cores = 9999
        process.command = ["/bin/ls", self._logdir]
        self.assertRaises(ttypes.RndException, core.ProcessMgr.runProcess, process)

    def testKillRunningTask(self):
        cmd = os.path.join(PLOW_ROOT, 'client/python/test/rndaemon/utils/cmds.py')

        process = self.getNewTaskCommand()
        process.command = [cmd, 'hard_to_kill']
        core.ProcessMgr.runProcess(process)
        time.sleep(1)

        runningTasks = core.ProcessMgr.getRunningTasks()
        total = len(runningTasks)
        self.assertEqual(total, 1, msg="Expected there to be one running task")

        core.ProcessMgr.killRunningTask(runningTasks[0])
        time.sleep(1)

        count = len(core.ProcessMgr.getRunningTasks())
        self.assertEqual(count, 0, msg="Should not have any running tasks anymore")

    def getNewTaskCommand(self):
        process = ttypes.RunTaskCommand()
        process.procId = "a"
        process.taskId = "b"
        process.cores = 1
        process.env = {},
        process.logFile = self._logfile 
        return process

class TestCommunications(unittest.TestCase):
    """
    Creates a mock server to accept communication tests 
    from the client API 
    """

    def setUp(self):
        from plow.rndaemon.server import get_server
        from plow.rndaemon.rpc import RndServiceApi 

        self.event = Event()

        self.server_port = 9092

        handler = ServiceHandler(self.event)
        self.server = get_server(RndServiceApi, handler, self.server_port)

        self.t_server = Process(target=self.server.serve)
        self.t_server.daemon = True
        self.t_server.start()
        time.sleep(.1)


    def tearDown(self):
        self.server.serverTransport.close()
        self.t_server.terminate()
        time.sleep(.1)


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


