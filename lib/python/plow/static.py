
import rpc.ttypes as ttypes

from client import Conn

__all__ = [
    "submitJobSpec",
    "findJobs"
]

def submitJobSpec(jobspec):
    Conn.service.launch(jobspec)

def findJobs(**kwargs):
    filt = ttypes.JobFilter()
    for k, v in kwargs.items():
        setattr(filt, k, v)
    return Conn.service.getJobs(filt)


