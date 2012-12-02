import unittest

import manifest
import plow

class StaticModuletests(unittest.TestCase):

    def testFindJobs(self):
        plow.getJobs()


    def testGetGroupedJobs(self):

        result = [
            {"id": 1, "parent":0, "name": "High"},
            {"id": 2, "parent":1, "name": "Foo"}
        ]

        for p in result:
            print p




if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(StaticModuletests)
    unittest.TextTestRunner(verbosity=2).run(suite)
