#!/usr/bin/env python

import os
import sys

sys.path.append(os.path.join(os.path.dirname(__file__), '../../../'))

import unittest

from plow.rndaemon.profile import SystemProfiler

import logging
logging.basicConfig(level=logging.INFO)


class TestSystemProfiler(unittest.TestCase):
    pass



if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(TestSystemProfiler)
    unittest.TextTestRunner(verbosity=2).run(suite)



