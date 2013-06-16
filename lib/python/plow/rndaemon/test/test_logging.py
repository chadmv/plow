#!/usr/bin/env python

import os
import pwd
import unittest
import tempfile
import uuid 

from plow.rndaemon.utils import ProcessLog
from plow.rndaemon.rpc.ttypes import RunTaskResult, RunTaskCommand

import logging
logging.basicConfig(level=logging.INFO)


class TestProcessLog(unittest.TestCase):

    def setUp(self):
        self._logfile = tempfile.NamedTemporaryFile(prefix='plow-rndaemon-test', delete=False).name
        logging.info("Using log: %s", self._logfile)

    def testHeaderFooter(self):
        task = self.getNewTaskCommand()
        result = self.getResult(task)

        log = ProcessLog(task.logFile)
        log.writeLogHeader(task)
        log.write("Foo\n")
        log.writeLogFooterAndClose(result)

    def testExtraAttributes(self):
        task = self.getNewTaskCommand()
        result = self.getResult(task)

        attrs = {'str': 'bar', 'int': 2, 'list': ['a','b',3], 'dict': {'val': 'result'}}
        log = ProcessLog(task.logFile)
        log.writeLogFooterAndClose(result, attrs)

        i = 0
        with open(task.logFile) as f:
            for line in f:
                line = line.strip()
                if line.startswith('int:'):
                    self.assertEqual(line, 'int: 2')
                elif line.startswith('str:'):
                    self.assertEqual(line, 'str: bar')
                elif line.startswith('dict:'):
                    self.assertEqual(line, "dict: {'val': 'result'}")
                elif line.startswith('list:'):
                    self.assertEqual(line, "list: ['a', 'b', 3]")
                else:
                    continue

                i += 1

        match = len(attrs)
        self.assertEqual(i, match, "Expected to match %d lines. Got %d" % (match, i))
                                                            
    def getNewTaskCommand(self):
        path = tempfile.NamedTemporaryFile(prefix='plow-rndaemon-test', delete=False).name

        process = RunTaskCommand()
        process.command = ['sleep', '1']
        process.procId = uuid.uuid4()
        process.taskId = uuid.uuid4()
        process.cores = 1
        process.uid = os.geteuid()
        process.username = pwd.getpwuid(process.uid).pw_name
        process.env = {}
        process.logFile = self._logfile 

        return process

    def getResult(self, rtc):
        result = RunTaskResult()
        result.procId = rtc.procId 
        result.taskId = rtc.taskId 
        result.jobId = rtc.jobId
        result.maxRssMb = 1024
        result.exitStatus = 0
        result.exitSignal = 0
        return result


if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(TestProcessLog)
    unittest.TextTestRunner(verbosity=2).run(suite)
