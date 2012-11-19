import os

import rpc.ttypes as ttypes

from client import Conn

__all__ = [
    "getJobs",
    "getActiveJob",
    "getJobById",
    "killJob",
    "launchJob",
    "pauseJob",
    "getJobOutputs",
    "getLayerById",
    "getLayerByName",
    "addLayerOutput",
    "getTaskById",
    "getTasks",
    "getNodes"
]

def getJobs(**kwargs):
    filt = ttypes.JobFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getJobs(filt)

def getActiveJob(name):
    return Conn.service.getActiveJob(name)

def getJobById(guid):
    return Conn.service.getJob(guid)

def killJob(job):
    Conn.service.killJob(job.id, "Manually killed by UID: %d " % os.getuid())

def launchJob(jobspec):
    return Conn.service.launch(jobspec)

def pauseJob(job, paused):
    Conn.service.pauseJob(job.id, paused)

def getJobOutputs(job):
    return Conn.service.getJobOutputs(job.id)

# Layers

def getLayerById(guid):
    return Conn.service.getLayerById(guid)

def getLayerByName(jobid, name):
    return Conn.service.getLayer(jobid, name)

def getLayerOutputs(job):
    return Conn.service.getLayerOutputs(job.id)

def addLayerOutput(layer, path, attrs=None):
    if attrs is None:
        attrs = dict()
    Conn.service.addOutput(str(layer.id), str(path), attrs)

# Tasks

def getTaskById(guid):
    return Conn.service.getTask(guid)

def getTasks(**kwargs):
    filt = ttypes.TaskFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getTasks(filt)

# Nodes

def getNodes(**kwargs):
    filt = ttypes.NodeFilterT()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getNodes(filt)


