import logging
logging.basicConfig(level=logging.DEBUG)

import os
import unittest

from blueprint.app import PluginManager
import blueprint.modules.shell as shell

class PluginManagerTests(unittest.TestCase):

    def testActivePluginsLoaded(self):
        """Check that the test plugin is loaded at startup."""
        self.assertEquals(1, len(PluginManager.getLoadedPlugins()))
        self.assertEquals(1, len(PluginManager.getActivePlugins()))

    def testSetup(self):
        """Verify the plugin setup() is called."""
        import blueprint.plugins.test as test_plugin
        self.assertTrue(test_plugin.Init.Setup)

    def testInitOnLayer(self):
        """Verify layer initialization works."""
        import blueprint.plugins.test as test_plugin
        l = shell.Shell("test", cmd=["/bin/ls"])
        self.assertTrue(l in test_plugin.Init.Layer)

if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (PluginManagerTests,):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)



