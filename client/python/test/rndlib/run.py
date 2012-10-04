import sys
sys.path.append("../../")

import unittest
import time

import rndlib
import rndlib.rpc.ttypes as ttypes
import rndlib.core

import logging
logging.basicConfig(level=logging.INFO)

class RndlibTests(unittest.TestCase):

    def testRunProcessCommand(self):
        process = ttypes.RunProcessCommand()
        process.procId = "a"
        process.frameId=  "b"
        process.cores = 1
        process.command=["/bin/ls", "/tmp"]
        process.env = { },
        process.logFile = "/tmp/rndlogfile.log"
        rndlib.core.runProcess(process)
        time.sleep(1)

    def testRunProcessCommandOutOfCores(self):
        process = ttypes.RunProcessCommand()
        process.procId = "a"
        process.frameId=  "b"
        process.cores = 3
        process.command=["/bin/ls", "/tmp"]
        process.env = { },
        process.logFile = "/tmp/rndlogfile.log"
        rndlib.core.runProcess(process)

if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(RndlibTests)
    unittest.TextTestRunner(verbosity=2).run(suite)




