#!/usr/bin/env python
import logging
import sys
sys.path.append("../client/python")

import rndlib.server

logging.basicConfig(level=logging.INFO)

if __name__ == "__main__":
    rndlib.server.start()

