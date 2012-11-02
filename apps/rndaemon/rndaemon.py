#!/usr/bin/env python
import logging
import sys

lvl = logging.DEBUG if '-debug' in sys.argv else logging.INFO
logging.basicConfig(level=lvl)

import sys
sys.path.append("../../client/python")

import plow.rndaemon.server as server

if __name__ == "__main__":
    try:
        server.start()
    except KeyboardInterrupt:
        sys.exit(2)

