#!/usr/bin/env python

import os
import sys

import tempfile
import unittest
import time
import math
import re 
import platform

from multiprocessing import Process, Event 
from ast import literal_eval 

from plowapp.rndaemon import conf 
conf.NETWORK_DISABLED = True

from plowapp.rndaemon.rpc import ttypes, RndServiceApi
from plowapp.rndaemon import core, server, client

import logging
logging.basicConfig(level=logging.DEBUG)

conf.TASK_PROXY_USER = os.getenv('PLOW_PROXY_USER', conf.TASK_PROXY_USER)

CMDS_UTIL = os.path.join(os.path.dirname(__file__), 'utils/cmds.py')

IS_LINUX = platform.system() in ('FreeBSD', 'Linux')


class TestResourceManager(unittest.TestCase):

    def testCoreCheckout(self):
        manager = core.ResourceMgr
        totalCores = core.Profiler.physicalCpus

        slots = len(manager.getSlots())
        self.assertEqual(totalCores, slots)

        slots = len(manager.getOpenSlots())
        self.assertEqual(totalCores, slots)

        slots = []
        for i in xrange(1, totalCores+1):
            slots += manager.checkout(1)
            total = totalCores - i
            openslots = len(manager.getOpenSlots())
            self.assertEqual(total, openslots)

        manager.checkin(slots)
        openslots = len(manager.getOpenSlots())
        self.assertEqual(totalCores, openslots)


class TestProcessManager(unittest.TestCase):

    _logdir = tempfile.gettempdir()
    _totalCores = core.Profiler.physicalCpus

    def setUp(self):
        self._logfile = tempfile.mktemp('.log', 'plow-test-')

    def tearDown(self):
        # give these types of tests a moment to close down
        time.sleep(.5)

    def testRunTaskCommand(self):
        process = self.getNewTaskCommand()
        process.command = [CMDS_UTIL, 'cpu_affinity']
        core.ProcessMgr.runProcess(process)
        
        while core.ProcessMgr.getRunningTasks():
            time.sleep(.1)

        captured_affinity = tuple(self.getLogCpuAffinity(process.logFile))
        count = len(captured_affinity)
        self.assertTrue(count == 1, "Expected only 1 result. Got %d" % count)

        if IS_LINUX:
            captured = captured_affinity[0]
            cpu_set = set()
            logical_cpus = core.Profiler.cpuprofile.logical_cpus

            for i in xrange(process.cores):
                cpu_set.update(logical_cpus[i])

            cpu_tuple = tuple(cpu_set)
            self.assertEqual(captured, cpu_tuple, 
                'Captured cpu affinity %s does not match expected %s' % (cpu_tuple, captured))


    def testRunTaskCommandHalfCores(self):
        if self._totalCores < 3:
            return

        cores = int(math.ceil(self._totalCores * .5))

        process = self.getNewTaskCommand()
        process.cores = cores
        process.command = [CMDS_UTIL, 'cpu_affinity']

        core.ProcessMgr.runProcess(process)


    def testRunTaskCommandMaxCores(self):
        process = self.getNewTaskCommand()
        process.cores = self._totalCores
        process.command = [CMDS_UTIL, 'cpu_affinity']

        core.ProcessMgr.runProcess(process)


    def testRunTaskCommandOutOfCores(self):
        process = self.getNewTaskCommand()
        process.cores = self._totalCores + 1
        process.command = ["/bin/ls", self._logdir]
        self.assertRaises(ttypes.RndException, core.ProcessMgr.runProcess, process)


    def testKillRunningTask(self):
        process = self.getNewTaskCommand()
        process.command = [CMDS_UTIL, 'hard_to_kill']
        core.ProcessMgr.runProcess(process)
        time.sleep(1)

        runningTasks = core.ProcessMgr.getRunningTasks()
        total = len(runningTasks)
        self.assertEqual(total, 1, msg="Expected there to be one running task")

        task = runningTasks[0]
        core.ProcessMgr.killRunningTask(task.procId)
        time.sleep(1)

        count = len(core.ProcessMgr.getRunningTasks())
        self.assertEqual(count, 0, msg="Should not have any running tasks anymore")

        sig, status = self.getLogSignalStatus(process.logFile)
        self.assertEqual(status, 1, "Expected a 1 Exit Status, but got %s" % status)
        self.assertEqual(sig, -9, "Expected a -9 Signal, but got %s" % sig)


    def getNewTaskCommand(self):
        process = ttypes.RunTaskCommand()
        process.procId = "a"
        process.taskId = "b"
        process.cores = 1
        process.env = {}
        process.logFile = self._logfile 
        return process


    @staticmethod
    def getLogSignalStatus(logfile):
        status = None
        signal = None
        status_field = 'Exit Status:'
        signal_field = 'Signal:'

        with open(logfile) as f:
            for line in f:
                if line.startswith(status_field):
                    try: status = int(line.split(status_field, 1)[-1])
                    except: pass
                elif line.startswith(signal_field):
                    try: signal = int(line.split(signal_field, 1)[-1])
                    except: pass

        return signal, status 

    @staticmethod 
    def getLogCpuAffinity(logfile):
        affinity = set()

        with open(logfile) as f:
            for line in f:
                match = re.search(r'cpu_affinity == (\([\d, ]+\))', line)
                if match:
                    try:
                        cpus = literal_eval(match.group(1))
                    except:
                        continue
                    affinity.add(cpus)

        return affinity



class TestCommunications(unittest.TestCase):
    """
    Creates a mock server to accept communication tests 
    from the client API 
    """

    def setUp(self):
        self.event = Event()

        self.server_port = 9092

        handler = ServiceHandler(self.event)
        self.server = server.get_server(RndServiceApi, handler, self.server_port)

        self.t_server = Process(target=self.server.serve)
        self.t_server.daemon = True
        self.t_server.start()
        time.sleep(.1)


    def tearDown(self):
        self.t_server.terminate()
        time.sleep(.5)


    def testSendPing(self):
        """
        Get a connection to the local "server" and test 
        that it receives a ping.
        """
        ping = ttypes.Ping()
        ping.hw = ttypes.Hardware()

        service, transport = client.getPlowConnection('localhost', self.server_port)
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
    for t in (TestCommunications, TestResourceManager, TestProcessManager):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)


