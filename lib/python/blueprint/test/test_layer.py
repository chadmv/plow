
import os
import json
import unittest
import logging

import blueprint

from common import TestLayer, TestTask

class LayerTests(unittest.TestCase):

    def setUp(self):
        self.job = blueprint.Job("test")
        self.layer = blueprint.Layer("test")

    def testCreateAndGet(self):
        """Create a new layer and add it to a job in non-script mode."""
        self.job.addLayer(self.layer)
        self.assertEquals(self.layer, self.job.getLayer("test"))
        self.assertEquals("test", self.layer.getName())

    def testGetSetArgs(self):
        """Test Get/Set args"""
        value = (1,2,3)
        self.layer.setArg("foo", value)
        self.assertEquals(value, self.layer.getArg("foo"))
        self.assertTrue(self.layer.isArgSet("foo"))
        self.assertFalse(self.layer.isArgSet("bar"))

    def testAddDepend(self):
        """Testing adding a dependency."""
        self.assertEquals(0, len(self.layer.getDepends()))
        other = blueprint.Layer("test2")
        self.layer.dependOn(other, blueprint.DependType.All)
        self.assertEquals(1, len(self.layer.getDepends()))

    def testAddDependByTaskWithConstructor(self):
        """Test setup depend by task with constructor."""
        l1 = TestLayer("testLayerA")
        l2 = TestLayer("testLayerB", depend="testLayerA")
        self.assertEquals(blueprint.DependType.ByTask, l2.getDepends()[0].type)

    def testAddDependAllWithConstructor(self):
        """Test setup depend:all with constructor"""
        l1 = TestLayer("testLayerA")
        l2 = TestLayer("testLayerB", depend="testLayerA:all")
        self.assertEquals(blueprint.DependType.All, l2.getDepends()[0].type)

    def testAddOutput(self):
        """Test adding an output."""
        self.assertEquals(0, len(self.layer.getOutputs()))
        self.layer.addOutput("comp", "/foo/bar.#.dpx")
        self.layer.getOutput("comp")
        self.assertEquals(1, len(self.layer.getOutputs()))

    def testAddInput(self):
        """Test adding an input."""
        self.assertEquals(0, len(self.layer.getInputs()))
        self.layer.addInput("scene", "/foo/bar.blender")
        self.layer.getInput("scene")
        self.assertEquals(1, len(self.layer.getInputs()))

    def testAfterInit(self):
        """Test that after_init is being run by the metaclass."""
        l = TestLayer("test2")
        self.assertTrue(l.afterInitSet)

    def testSetup(self):
        """Test that _setup is being called."""
        l = TestLayer("test2")
        self.assertFalse(l.setupSet)
        l.setup()
        self.assertTrue(l.setupSet)

    def testExecute(self):
        """Test that _execute is being called."""
        l = TestLayer("test2")
        self.assertFalse(l.executeSet)
        l.execute()
        self.assertTrue(l.executeSet)

    def testBeforeExecute(self):
        """Test that _beforeExecute is being called."""
        l = TestLayer("test2")
        self.assertFalse(l.beforeExecuteSet)
        l.beforeExecute()
        self.assertTrue(l.beforeExecuteSet)

    def testAfterExecute(self):
        """Test that _afterExecute is being called."""
        l = TestLayer("test2")
        self.assertFalse(l.afterExecuteSet)
        l.afterExecute()
        self.assertTrue(l.afterExecuteSet)


class TaskTests(unittest.TestCase):

    def testCreateTask(self):
        t = TestTask("test")





if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (LayerTests, TaskTests):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)



