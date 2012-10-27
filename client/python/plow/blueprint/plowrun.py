import os
import yaml
import getpass

import plow.rpc.ttypes as ttypes
import plow.conf as conf 

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol

from plow.rpc import RpcServiceApi

class BlueprintRunner(object):
    def __init__(self, **kwargs):
        self.__args = {
            "paused": False
        }
        self.__args.update(kwargs)

    def setArg(self, key, value):
        self.__args[key] = value

    def getArg(self, key, default=None):
        self.__args.get(key, default)

    def run(self, job):

        bp = toBlueprint(job, **self.__args)
        getPlowService().launch(bp)

def plowrun(job, **kwargs):
    bpr = BlueprintRunner(**kwargs)
    bpr.run(job)

def getPlowService():
    socket = TSocket.TSocket("localhost", 11336)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RpcServiceApi.Client(protocol)
    transport.open()
    return service

def toBlueprint(job, **kwargs):

    frange = kwargs.get("frame_range", "1001")

    bp = ttypes.Blueprint()
    bp.job = ttypes.JobBp()
    bp.job.project = "test";
    bp.job.username = getpass.getuser()
    bp.job.uid = os.getuid()
    bp.job.paused = kwargs.get("paused", False)
    bp.job.name = job.getName()
    bp.job.logPath = conf.get("blueprint", "log_path");
    bp.layers = []

    for layer in job.getLayers():
        bpl = ttypes.LayerBp()
        bpl.name = layer.getName()
        bpl.command = [
            os.path.join(conf.get('env', 'plow_root'), "tools/taskrun/taskrun"),
            os.path.join(job.getPath(), "blueprint.yaml"),
            "-layer",
            layer.getName(),
            "-range",
            "%{FRAME}"
        ]
        bpl.tags =  layer.getArg("tags", ["unassigned"])
        bpl.range = layer.getArg("frame_range", frange)
        bpl.chunk = layer.getArg("chunk", 1)
        bpl.minCores = layer.getArg("min_threads", 1)
        bpl.maxCores = layer.getArg("max_threads", 0)
        bpl.minMemory = layer.getArg("min_ram", 256)
        bp.layers.append(bpl)

    return bp

"""


struct LayerBp {
    1:string name,
    2:list<string> command;
    3:set<string> tags,
    4:string range,
    5:i32 chunk,
    6:i32 minCores,
    7:i32 maxCores,
    8:i32 minMemory
}

struct JobBp {
    1:string name,
    2:string project,
    3:i32 uid,
    4:bool paused
    5:list<LayerBp> layers
}
"""


