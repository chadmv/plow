import os
import yaml

import plow.rpc.ttypes as ttypes

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

    def set_arg(self, key, value):
        self.__args[key] = value

    def get_arg(self, key, default=None):
        self.__args.get(key, default)

    def run(self, job):

        bp = to_blueprint(job, **self.__args)
        plow_conn = get_plow_conn()
        plow_conn.launch(bp)

def plowrun(job, **kwargs):
    bpr = BlueprintRunner(**kwargs)
    bpr.run(job)

def get_plow_conn():
    socket = TSocket.TSocket("localhost", 11336)
    transport = TTransport.TFramedTransport(socket)
    protocol = TBinaryProtocol.TBinaryProtocol(transport)
    service = RpcServiceApi.Client(protocol)
    transport.open()
    return service

def to_blueprint(job, **kwargs):

    frange = kwargs.get("frame_range", "1001")

    bp = ttypes.Blueprint()
    bp.job = ttypes.JobBp()
    bp.job.project = "test";
    bp.job.uid = os.getuid()
    bp.job.paused = kwargs.get("paused", False)
    bp.job.name = job.get_name()
    bp.layers = []

    for layer in job.get_layers():
        bpl = ttypes.LayerBp()
        bpl.name = layer.get_name()
        bpl.command = ["/Users/chambers/src/plow/tools/taskrun/taskrun",
                       os.path.join(job.get_path(), "blueprint.yaml"),
                       "-layer",
                       layer.get_name(),
                       "-range",
                       "%{FRAME}"]
        bpl.tags =  layer.get_arg("tags", ["unassigned"])
        bpl.range = layer.get_arg("frame_range", frange)
        bpl.chunk = layer.get_arg("chunk", 1)
        bpl.minCores = layer.get_arg("min_threads", 1)
        bpl.maxCores = layer.get_arg("max_threads", 0)
        bpl.minMemory = layer.get_arg("min_ram", 256)
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


