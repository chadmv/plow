#!/usr/bin/env python

import sys

sys.path.append("../client/python")

import rndlib.server
import rndlib.rpc.ttypes as ttypes
from rndlib.core import LibRndApi

if __name__ == "__main__":

    process = ttypes.RunProcessCommand()
    process.procId = "a"
    process.frameId=  "b"
    process.cores = 1
    process.command=["/bin/ls", "/tmp"]
    process.env = { },
    process.logFile = "/tmp/rndlogfile.log"

    rnd = LibRndApi()
    rnd.launch(process)




