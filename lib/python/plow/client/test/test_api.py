#!/usr/bin/env python
import os
import unittest
import uuid

import manifest
import plow.client

def launch_test_job(name):
    """
    struct JobSpecT {
    1:string name,
    2:string project,
    3:bool paused,
    4:string username,
    5:i32 uid,
    6:string logPath
    7:list<LayerSpecT> layers,
    8:list<DependSpecT> depends
    
    1:string name,
    2:list<string> command,
    3:set<string> tags,
    4:optional string range,
    5:i32 chunk = 1,
    6:i32 minCores = 1,
    7:i32 maxCores = 1,
    8:i32 minRamMb = 1024,
    9:bool threadable = false,
    10:list<DependSpecT> depends,
    11:list<TaskSpecT> tasks

    """

    spec = plow.client.JobSpecT()
    spec.name = name
    spec.project = "test"
    spec.paused = True
    spec.username = os.environ["USER"]
    spec.uid = os.geteuid()
    spec.logPath = "/tmp"
    spec.layers = []
    spec.depends = []

    layer = plow.client.LayerSpecT()
    layer.name = "test_layer"
    layer.command = ["/bin/ls"]
    layer.tags = ["unassigned"]
    layer.range = "1-1"

    spec.layers.append(layer)

    return plow.client.launch_job(spec)

def clear_job(name):
    try:
        job = plow.client.get_active_job(name)
        plow.client.kill_job(job)
    except:
        pass

class ApiModuleTests(unittest.TestCase):

    def test_get_jobs(self):
        clear_job("test_job_1")
        job = None
        try:
            job = launch_test_job("test_job_1")
            self.assertTrue(job.id in {j.id for j in plow.client.get_jobs()})
        finally:
            if job:
                plow.client.kill_job(job)

    def test_get_job(self):
        clear_job("test_job_2")
        job1 = None
        try:
            job1 = launch_test_job("test_job_2")
            job2 = plow.client.get_job(job1.id)
            self.assertEquals(job1.id, job2.id)
        finally:
            if job1:
                plow.client.kill_job(job1)

    def test_get_active_job(self):
        clear_job("test_job_3")
        job1 = None
        try:
            job1 = launch_test_job("test_job_3")
            job2 = plow.client.get_active_job(job1.name)
            self.assertEquals(job1.id, job2.id)
        finally:
            if job1:
                plow.client.kill_job(job1)

    def test_get_clusters(self):
        clusters = plow.client.get_clusters()
        self.assertTrue(len(clusters) > 0)

    def test_get_cluster(self):
        c1 = plow.client.get_cluster("unassigned")
        c2 = plow.client.get_cluster(c1.id)
        self.assertEquals(c1, c2)

    def test_create_cluster(self):
        name = str(uuid.uuid4())
        c = plow.client.create_cluster(name, ["linux", "himem"])
        self.assertEquals(name, c.name)

    def test_delete_cluster(self):
        name = str(uuid.uuid4())
        c = plow.client.create_cluster(name, ["linux", "himem"])
        plow.client.delete_cluster(c)
        try:
            plow.client.get_cluster(name)
        except Exception, e:
            pass

if __name__ == "__main__":
    suite = unittest.TestLoader().loadTestsFromTestCase(ApiModuleTests)
    unittest.TextTestRunner(verbosity=2).run(suite)
