#!/usr/bin/env python

"""
cmds.py 

Utility for launching test jobs with a number of 
child processes. 
"""

import os
import sys
import subprocess
import platform
from signal import signal, SIGTERM

import psutil 

import logging

from plowapp.rndaemon.rpc.ttypes import RndException

logging.basicConfig(
    format='PID %(process)s::%(funcName)s:\t%(message)s',
    level=logging.INFO
)


def easy_to_kill(*args):
    """
    Exits nicely on a SIGTERM
    """
    def handler(*args2):
        logging.info("Received SIGTERM: I will stop now")
        sys.exit(SIGTERM)

    signal(SIGTERM, handler)
    do_sleep(3)


def hard_to_kill(*args):
    """
    Ignores a SIGTERM and keeps running
    """
    def ignore(*args2):
        logging.info("Received SIGTERM: Not gunna die easily!! Ignoring.")

    signal(SIGTERM, ignore)
    do_sleep(2)


def cpu_affinity(*args):
    """

    """
    if platform.system() not in ('Linux', 'FreeBSD'):
        cpus = tuple(xrange(psutil.NUM_CPUS))
        logging.warn('(Platform Unsupported; Showing all) cpu_affinity == %s', cpus)
        return

    aff = psutil.Process(os.getpid()).get_cpu_affinity()
    logging.info('cpu_affinity == %s', tuple(aff))
    spawn(1)


def echo_log(log, *args):
    """
    Echo a log file, line by line, to stdout
    """
    with open(log) as f:
        for line in f:
            sys.stdout.write(line)
            sys.stdout.flush()


def crashing(*args):
    """
    Purposely crashes and exits non-zero
    """
    raise RndException(what=1, why="Simulating a crash")


def do_sleep(spawn_more=0):
    """
    do_sleep(int spawn_more=0)

    Does a sleep in a subprocess, and optionally spawns 
    more instances of the main process before waiting. 

    `spawn_more` is an int number of extra sleeps to also spawn
    """
    p = subprocess.Popen(['sleep', '30'])
    logging.info("Sleeping")
    if spawn_more > 0:
        spawn(spawn_more)
    try:
        p.wait()
    except:
        pass


def spawn(num=1):
    """
    spawn(int num=1)

    spawn `num` amount of instances of the main app, 
    using the same arguments, in child processes
    """
    if __name__ == "__main__" and len(sys.argv) < 3:
        processes = []
        for i in xrange(num):
            p = subprocess.Popen(sys.argv + ['as_child'])
            logging.info("Started #%d child PID => %s" % (i, p.pid))
            processes.append(p)


# available command line args
COMMANDS = (
    easy_to_kill,
    hard_to_kill,
    cpu_affinity,
    echo_log,
    crashing,
)


def fail():
    print "Usage: %s <command> [arg1, ...]" % sys.argv[0]
    print [c.__name__ for c in COMMANDS]
    sys.exit(255)


if __name__ == "__main__":
    try:
        cmdName = sys.argv[1]
    except IndexError:
        fail()

    args = sys.argv[2:]

    for fn in COMMANDS:
        if fn.__name__ == cmdName:
            logging.info("Running as UID,GID == %d,%d", os.getuid(), os.getgid())
            fn(*args)

            sys.exit(0)

    # no valid commands passed
    fail() 
