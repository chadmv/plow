#!/usr/bin/env python
import logging
logging.basicConfig(level=logging.INFO)

import sys
sys.path.append("../../client/python")

import rndlib.server

if __name__ == "__main__":
    rndlib.server.start()

