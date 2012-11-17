import unittest

import blueprint

from common import TestLayer, TestTask

class TaskrunTests(unittest.TestCase):

    def setUp(self):
        self.job = blueprint.Job("test")

    def testRunTask(self):
        task = TestTask("test")
        self.job.addLayer(task)
        self.job.setup()
        task.execute()

if __name__ == "__main__":
    suite = unittest.TestSuite()
    for t in (TaskrunTests,):
        suite.addTest(unittest.TestLoader().loadTestsFromTestCase(t))
    unittest.TextTestRunner(verbosity=2).run(suite)


