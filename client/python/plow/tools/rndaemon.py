#!/usr/bin/env python

import sys
import logging


def main():

    import argparse

    parser = argparse.ArgumentParser(
        description='Start the Plow Render Node Daemon',
        usage='%(prog)s [opts]',
    )

    parser.add_argument("-debug", action="store_true", 
        help="Print more debugging output")

    args = parser.parse_args()  

    lvl = logging.DEBUG if args.debug else logging.INFO
    logging.basicConfig(level=lvl)

    import plow.rndaemon.server as server

    try:
        server.start()
    except KeyboardInterrupt:
        sys.exit(2)

if __name__ == "__main__":
    main()

