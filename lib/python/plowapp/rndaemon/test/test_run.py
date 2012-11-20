#!/usr/bin/env python

import os
import tempfile
import unittest
import time
import math
import re 
import platform

from functools import partial
from multiprocessing import Process, Event 
from ast import literal_eval 

import psutil

from plowapp.rndaemon import conf 
conf.NETWORK_DISABLED = True

from plowapp.rndaemon.rpc import ttypes, RndServiceApi
from plowapp.rndaemon import core, server, client, utils

import logging
logging.basicConfig(level=logging.DEBUG)

conf.TASK_PROXY_USER = os.getenv('PLOW_PROXY_USER', conf.TASK_PROXY_USER)

CMDS_UTIL = os.path.join(os.path.dirname(__file__), 'utils/cmds.py')
DATA_DIR = os.path.join(os.path.dirname(__file__), 'data')

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
        for i in xrange(1, totalCores + 1):
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
        self._processmgr_processFinished = core.ProcessMgr.processFinished

    def tearDown(self):
        # give these types of tests a moment to close down
        time.sleep(1)
        core.ProcessMgr.processFinished = self._processmgr_processFinished

    def testRunTaskCommand(self):
        process = self.getNewTaskCommand()
        process.command = [CMDS_UTIL, 'cpu_affinity']
        core.ProcessMgr.runProcess(process)

        while core.ProcessMgr.getRunningTasks():
            time.sleep(.1)

        sig, status = self.getLogSignalStatus(process.logFile)
        self.assertEqual(status, 0, "Expected a 0 Exit Status, but got %s" % status)

        self.cpuAffinityTestUtil(process)

    def testRunTaskCommandHalfCores(self):
        if self._totalCores < 3:
            return

        cores = int(math.ceil(self._totalCores * .5))

        process = self.getNewTaskCommand()
        process.cores = cores
        process.command = [CMDS_UTIL, 'cpu_affinity']

        core.ProcessMgr.runProcess(process)

        while core.ProcessMgr.getRunningTasks():
            time.sleep(.1)

        self.cpuAffinityTestUtil(process)

    def testRunTaskCommandMaxCores(self):
        process = self.getNewTaskCommand()
        process.cores = self._totalCores
        process.command = [CMDS_UTIL, 'cpu_affinity']

        core.ProcessMgr.runProcess(process)

        while core.ProcessMgr.getRunningTasks():
            time.sleep(.1)

        self.cpuAffinityTestUtil(process)

    def testRunTaskCommandOutOfCores(self):
        process = self.getNewTaskCommand()
        process.cores = self._totalCores + 1
        process.command = ["/bin/ls", self._logdir]
        self.assertRaises(ttypes.RndException, core.ProcessMgr.runProcess, process)

    def testKillRunningTask(self):
        process = self.getNewTaskCommand()
        # process.command = [CMDS_UTIL, 'hard_to_kill']
        process.command = [
            'taskrun',
             '-debug',
             '-task',
             'hard_to_kill_job',
             os.path.join(DATA_DIR, 'hard_kill.bp')
        ]
        core.ProcessMgr.runProcess(process)
        time.sleep(1)

        runningTasks = core.ProcessMgr.getRunningTasks()
        total = len(runningTasks)
        self.assertEqual(total, 1, msg="Expected there to be one running task")

        task = runningTasks[0]
        core.ProcessMgr.killRunningTask(task.procId, "Killing for testing reasons")
        time.sleep(1)

        count = len(core.ProcessMgr.getRunningTasks())
        self.assertEqual(count, 0, 
            msg="Expected 0 running tasks but got %s" % count)

        i = 0
        while core.ProcessMgr.getRunningTasks():
            time.sleep(.25)
            self.assertTrue(i < 10, 
                "Tasks are still running when they should be dead by now")
            i += 1

        sig, status = self.getLogSignalStatus(process.logFile)
        self.assertEqual(status, 1, "Expected a 0 Exit Status, but got %s" % status)
        self.assertEqual(sig, -9, "Expected a -9 Signal, but got %s" % sig)



    def testFailedTask(self):
        D = {'result': None}

        def processFinished(d, rtc):
            d['result'] = rtc 
            self._processmgr_processFinished(rtc)

        core.ProcessMgr.processFinished = partial(processFinished, D)

        process = self.getNewTaskCommand()
        process.command = [
            'taskrun',
             '-debug',
             '-task',
             'crashing_job',
             os.path.join(DATA_DIR, 'crashing.bp')
        ]

        task = core.ProcessMgr.runProcess(process)
        ppid = task.pid

        try:
            psutil.Process(ppid).wait(5)
        except psutil.TimeoutExpired:
            self.fail("Task should not still be running: %s" % task)
        except psutil.NoSuchProcess:
            pass

        i = 0
        while core.ProcessMgr.getRunningTasks():
            time.sleep(.25)
            self.assertTrue(i < 10, 
                "Tasks are still running when they should be dead by now")
            i += 1

        sig, status = self.getLogSignalStatus(process.logFile)
        self.assertEqual(sig, 0)
        self.assertEqual(status, 1)

        self.assertTrue(D['result'] is not None, "Result was %r" % D)
        self.assertEqual(D['result'].exitStatus, 1)
        self.assertEqual(D['result'].exitSignal, 0)


    def testTaskProgress(self):
        # disable the callback 

        D = {'result': None}

        def processFinished(d, rtc):
            d['result'] = rtc 

        conf.TASK_PROGRESS_PATTERNS = {
            'blender': '^Fra:\\d+ .*? \\| Rendering \\| .*? (\\d+/\\d+)$',
            'mray': '^JOB[\\w. ]+:\\s+([\\d.]+%)\\s+'
        }

        core.ProcessMgr.processFinished = partial(processFinished, D)

        process = self.getNewTaskCommand()
        process.taskTypes = ['blender', 'mray']

        for log in ('blender.log', 'mentalRay.log'):
            process.command = [CMDS_UTIL, 'echo_log', os.path.join(DATA_DIR, log)]

            t = core._ProcessThread(process, cpus=[0])

            running = t.getRunningTask()
            self.assertEqual(running.progress, 0,
                'Initial progress for "%s" job should be 0' % log)

            running.lastLog = None
            repr(running)

            t.start()
            t.join()

            self.assertTrue(D['result'] is not None)
            self.assertEqual(D['result'].exitStatus, 0)

            running = t.getRunningTask()

            self.assertEqual(running.progress, 1,
                'Final progress for "%s" job should be 1. Got %s' \
                % (log, running.progress))

            D['result'] = None

    def getNewTaskCommand(self):
        process = ttypes.RunTaskCommand()
        process.procId = "a"
        process.taskId = "b"
        process.cores = 1
        process.env = {}
        process.logFile = self._logfile 
        return process

    def cpuAffinityTestUtil(self, process):
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

    @staticmethod
    def getLogSignalStatus(logfile):
        status = None
        signal = None
        status_field = 'Exit Status:'
        signal_field = 'Signal:'

        with open(logfile) as f:
            for line in f:
                if line.startswith(status_field):
                    try: 
                        status = int(line.split(status_field, 1)[-1])
                    except: 
                        pass
                elif line.startswith(signal_field):
                    try: 
                        signal = int(line.split(signal_field, 1)[-1])
                    except: 
                        pass

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

        handler = _ServiceHandler(self.event)
        self.server = server.get_server(RndServiceApi, handler, self.server_port)

        self.t_server = Process(target=self.server.serve)
        self.t_server.daemon = True
        self.t_server.start()
        time.sleep(.1)

    def tearDown(self):
        self.t_server.terminate()
        time.sleep(1)

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


class _ServiceHandler(object):

    def __init__(self, evt):
        self.event = evt 

    def sendPing(self, ping):
        self.event.set()


class TestLogParser(unittest.TestCase):

    def testProgressStatic(self):
        parser = utils.ProcessLogParser([
            '^Fra:\d+ .*? \| Rendering \| .*? (\d+/\d+)$',
            '^JOB[\w. ]+:\s+([\d.]+%)\s+'])

        logtests = {
            'blender.log': {
                'total': 42,
                'indexes': [(0, 0.0), (5, .4375), (20, .671875), (30, .828125), (-1, 1.0)]
            },
            'mentalRay.log': {
                'total': 300,
                'indexes': [(0, .003), (20, .07), (50, .17), (150, .503), (250, .836), (-1, 1.0)]
            }
        }

        for name, attribs in logtests.iteritems():
            log = os.path.join(DATA_DIR, name)

            progs = []
            for line in open(log):
                val = parser.parseProgress(line)
                if val is not None:
                    progs.append(val)

            total = attribs['total']
            found = len(progs)
            self.assertEqual(found, total, "Expected %d progress updates. Got %d" % (total, found))

            for idx, val in attribs['indexes']:
                self.assertEqual(progs[idx], val)


if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (TestCommunications, TestResourceManager, TestProcessManager):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)
