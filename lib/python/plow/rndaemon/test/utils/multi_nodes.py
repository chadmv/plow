#!/usr/bin/env python

import os
import sys
import time 
import logging
import socket

from multiprocessing import Process, current_process

from plow.rndaemon.main import RndFormatter
from plow.rndaemon import conf


class RndProcessHandler(object):

    def runTask(self, rtc):
        logger = logging.getLogger(__name__)
        logger.info("Server asked node to run a task: %r", rtc)

    def killRunningTask(self, procId, reason):
        logger = logging.getLogger(__name__)
        logger.info("Server asked node to kill a task on proc %r", procId)

    def getRunningTasks(self):
        logger = logging.getLogger(__name__)
        logger.info("Server asked node to report running tasks")
        return []

    def reboot(self, now=False):
        logger = logging.getLogger(__name__)
        logger.info("Server asked node to reboot (now = %r)", now)


def start(port, host=None):

    from plow.rndaemon import profile
    from plow.rndaemon.profile import test

    if host:
        test.HOST_NAME = host

    profile.SystemProfiler = test.SystemProfiler

    from plow.rndaemon.server import get_server
    from plow.rndaemon.rpc import RndNodeApi

    logger = logging.getLogger(current_process().name)
    logger.info("Staring Render Node Daemon on TCP port %d", port)
    server = get_server(RndNodeApi, RndProcessHandler(), port)

    try:
        server.serve()
    except KeyboardInterrupt:
        sys.exit(2)


if __name__ == "__main__":

    logger = logging.getLogger()
    ch = logging.StreamHandler()
    ch.setLevel(logging.DEBUG)
    formatter = RndFormatter(datefmt='%Y-%m-%d %H:%M:%S')
    ch.setFormatter(formatter)
    logger.addHandler(ch)

    logger.setLevel(logging.DEBUG)

    import argparse

    parser = argparse.ArgumentParser(
        description='Start the Plow Render Node Daemon',
        usage='%(prog)s [opts]',
    )

    parser.add_argument("-num", type=int, default=1,
        help="Number of rndaemon instances to start")

    parser.add_argument("-refresh", type=int, default=30,
        help="How often, in seconds, to ping in updated data for each node")

    parser.add_argument("-random", action='store_true', default=False,
        help="Randomize the host names, instead of using sequential numbers")

    args = parser.parse_args()  

    daemons = []

    num = max(args.num, 1)
    conf.NETWORK_PING_INTERVAL = max(args.refresh, 1)

    logger.info("Starting %d rndaemon processes...", num)

    host_base = socket.getfqdn()

    for i in xrange(num):
        name = 'rndaemon-instance-{0}'.format(i)
        
        if not args.random:
            host = "{0}.TEST.{1}".format(host_base, i)
        else:
            host = None 

        p = Process(target=start, args=(conf.NETWORK_PORT + i, host), name=name)
        p.daemon = True
        p.start()
        daemons.append(p)

    while True:
        try:
            time.sleep(.5)
        except KeyboardInterrupt:
            for d in daemons:
                d.terminate()
                d.join()
            break

    sys.exit(0)



