#!/usr/bin/env python

import sys
import logging


class RndFormatter(logging.Formatter):
    debug_fmt = "%(asctime)s %(levelname)-8s %(name)s:%(lineno)d - %(message)s"
    standard_fmt = "%(asctime)s %(levelname)-8s %(name)s - %(message)s"

    def format(self, record):

        orig = self._fmt 

        if record.levelno == logging.DEBUG:
            self._fmt = self.debug_fmt
        else:
            self._fmt = self.standard_fmt

        result = logging.Formatter.format(self, record)
        self._fmt = orig

        return result


def main():

    import argparse

    parser = argparse.ArgumentParser(
        description='Start the Plow Render Node Daemon',
        usage='%(prog)s [opts]',
    )

    parser.add_argument("-debug", action="store_true", 
        help="Print more debugging output")

    args = parser.parse_args()  

    logger = logging.getLogger()
    ch = logging.StreamHandler()
    ch.setLevel(logging.DEBUG)
    formatter = RndFormatter(datefmt='%Y-%m-%d %H:%M:%S')
    ch.setFormatter(formatter)
    logger.addHandler(ch)

    logger.setLevel(logging.DEBUG if args.debug else logging.INFO)

    import server

    try:
        server.start()
    except KeyboardInterrupt:
        sys.exit(2)


if __name__ == "__main__":
    main()
