import unittest

import manifest
import plow

class StaticModuletests(unittest.TestCase):

    def testFindJobs(self):
        plow.findJobs()


if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(StaticModuletests)
    unittest.TextTestRunner(verbosity=2).run(suite)
