#!/usr/bin/env python

import unittest

PREFIX = 'plow.test'

TESTS = (
    'rndaemon.test_profile',
    'rndaemon.test_run.TestCommunications',
    'rndaemon.test_run.TestResourceManager',
    'rndaemon.test_run.TestProcessManager',
)

def additional_tests():
    tests = ['.'.join([PREFIX, name]) for name in TESTS]
    suite = unittest.TestSuite()
    suite.addTest(unittest.TestLoader().loadTestsFromNames(tests))
    return suite


if __name__ == "__main__":
    suite = additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
