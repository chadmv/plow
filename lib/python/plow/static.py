
import rpc.ttypes as ttypes

from client import Conn

__all__ = [
    "submitJobSpec",
    "findJobs",
    "getLayer",
    "registerOutput"
]

def submitJobSpec(jobspec):
    return Conn.service.launch(jobspec)

def findJobs(**kwargs):
    filt = ttypes.JobFilter()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getJobs(filt)

def getLayer(*args):
    if len(args) == 1:
        return Conn.service.getLayerById(args[0])
    elif len(args) == 2:
        return Conn.service.getLayer(args[0], args[1])

def registerOutput(layer, path, attrs=None):
    if attrs is None:
        attrs = dict()
    Conn.service.addOutput(str(layer.id), str(path), attrs)


