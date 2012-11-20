import os
import json
import unittest
import logging

import blueprint.fileseq as fileseq
import blueprint.modules.blender as blender

logging.basicConfig(level=logging.DEBUG)

class BlenderModuleTests(unittest.TestCase):

    scene_file = os.path.join(os.path.dirname(__file__), "data/test.blend")

    def testInitialize(self):
        """Initialize a Blender instance."""
        b = blender.Blender("comp", scene_file=self.scene_file)
        b._setup()

        self.assertEquals(1, len(b.getSetupTasks()))
        self.assertEquals(1, len(b.getDepends()))

    def testExecuteSetupTask(self):
        """Execute the setup task."""
        b = blender.Blender("comp", scene_file=self.scene_file)
        b._setup()

        # Grab the setup task and execute it.
        b.getSetupTasks()[0]._execute()
        # Now the outputs should be available.
        outputs = b.getOutputs()
        # Assert we have two outputs.
        self.assertEquals(4, len(outputs))

    def testExecuteRenderTask(self):
        b = blender.Blender("comp", scene_file=self.scene_file)
        b._setup()
        b.getSetupTasks()[0]._execute()
        b._execute(fileseq.FrameSet("1-2"))

if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (BlenderModuleTests,):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)