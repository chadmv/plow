
import os
import json
import unittest
import logging

import blueprint

from common import TestLayer, TestTask

class PluginManagerTests(unittest.TestCase):

    def testInit(self):
        """Test that the test plugin is being initialized."""
        self.assertTrue(blueprint.plugins.test_plugin.Init.Loaded)

    def testAfterInit(self):
        """Test that after_init is being run by the plugin manager."""
        l = TestLayer("test2")
        self.assertTrue(blueprint.plugins.test_plugin.Init.AfterInit)

    def testSetup(self):
        """Test that setup is being run by the plugin manager."""
        l = TestLayer("test2")
        l.setup()
        self.assertTrue(blueprint.plugins.test_plugin.Init.Setup)

    def testBeforeExecute(self):
        """Test that before execute is being run by the plugin manager."""
        l = TestLayer("test2")
        l.beforeExecute()
        self.assertTrue(blueprint.plugins.test_plugin.Init.BeforeExecute)

    def testAfterExecute(self):
        """Test that after execute is being run by the plugin manager."""
        l = TestLayer("test2")
        l.afterExecute()
        self.assertTrue(blueprint.plugins.test_plugin.Init.AfterExecute)

if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (PluginManagerTests,):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)





