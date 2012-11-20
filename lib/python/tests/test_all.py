#!/usr/bin/env python

import unittest

import logging
logging.basicConfig(level=logging.INFO)

TESTS = []

# blueprint tests
TESTS += ['.'.join(['blueprint.test', p]) for p in (
    'test_app',
    'test_layer',
    # 'test_modules',  # this depends on blender. can't include it
    'test_taskrun',
    )
]

# rndaemon tests
TESTS += ['.'.join(['plowapp.rndaemon.test', p]) for p in (
    'test_profile',
    'test_run.TestCommunications',
    'test_run.TestResourceManager',
    'test_run.TestProcessManager',
    )
]

def additional_tests():
    suite = unittest.TestSuite()
    suite.addTest(unittest.TestLoader().loadTestsFromNames(TESTS))
    return suite


if __name__ == "__main__":
    suite = additional_tests()
    unittest.TextTestRunner(verbosity=2).run(suite)
