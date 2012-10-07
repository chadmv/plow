import unittest
import logging

import os
os.environ["PLOW_CFG"] = "./plow.ini"
os.environ["PROJECT"] = "test"
os.environ["SHOT"] = "test.01"

import sys
sys.path.append("../..")

import plow.blueprint as bp
import plow.blueprint.plowrun as plowrun

from plow.blueprint.modules.shell import Shell

logging.basicConfig(level=logging.DEBUG)

class BlueprintTests(unittest.TestCase):

    def test_exec_task(self):
        job = bp.Job("test_job")
        job.add_layer(Shell("ls", cmd=["/bin/ls", "/tmp"]))
        job.setup()

        plowrun.plowrun(job)



        



if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(BlueprintTests)
    unittest.TextTestRunner(verbosity=2).run(suite)

