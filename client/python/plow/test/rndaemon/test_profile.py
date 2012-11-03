#!/usr/bin/env python

import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '../../../'))

import unittest
import platform

from plow.rndaemon.profile import SystemProfiler

import logging
logging.basicConfig(level=logging.INFO)

DATA_DIR = os.path.join(os.path.dirname(__file__), 'data')


class TestSystemProfiler(unittest.TestCase):

    def testProfile(self):
        prof = SystemProfiler()

        cmd, opts = prof.getSubprocessOpts('some command')
        
        self.assertEqual(cmd, ['some', 'command'], 
            "String command should have been split into a list")

        self.assertTrue(isinstance(opts['env'], dict))

        for k,v in opts['env'].iteritems():
            self.assertTrue(isinstance(v, str), 
                "ENV key '%s' has a value %s of type %s instead of str()" % (k, v, type(v)))


    def testLinuxCpuProfile(self):

        from plow.rndaemon.profile.linux import CpuProfile 

        saved = CpuProfile.CPUINFO 
        CpuProfile.CPUINFO = os.path.join(DATA_DIR, 'cpuinfo.dat')

        cpu_profile = CpuProfile()

        CpuProfile.CPUINFO = saved

        self.assertEqual(cpu_profile.num_cpus, 16)
        self.assertEqual(cpu_profile.num_phys_cpus, 8)

        self.assertEqual(len(cpu_profile.physical_cpus), 2)

        cpu1 = cpu_profile.physical_cpus[0]
        self.assertTrue(cpu1['ht_enabled'])
        self.assertEqual(cpu1['num_cores'], 4)
        self.assertEqual(cpu1['ht_factor'], 2)

        expected_processors = {
            0   : set([1, 9]),
            1   : set([11, 3]),
            9   : set([5, 13]),
            10  : set([15, 7])
        }

        self.assertEqual(cpu1['processors'], expected_processors)

        cpu2 = cpu_profile.physical_cpus[1]

        expected_processors = {
            0   : set([0, 8]),
            1   : set([2, 10]),
            9   : set([12, 4]),
            10  : set([14, 6])
        }

        self.assertEqual(cpu2['processors'], expected_processors)

        self.assertEqual(len(cpu_profile.logical_cpus), 8,
            "Expected 8 cores")

        for s in cpu_profile.logical_cpus.itervalues():
            self.assertEqual(len(s), 2, 
                "There should be two processor ids in each set")        



if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(TestSystemProfiler)
    unittest.TextTestRunner(verbosity=2).run(suite)



