#!/usr/bin/env python

"""
cmds.py 

Utility for launching test jobs with a number of 
child processes. 
"""

import os
import time 
import sys
import subprocess
import platform
import multiprocessing as mp
from signal import signal, SIGTERM

import psutil 

import logging

logging.basicConfig(
	format='PID %(process)s::%(funcName)s:\t%(message)s', 
	level=logging.INFO)


def easy_to_kill():
	"""
	Exits nicely on a SIGTERM
	"""
	def handler(*args):
		logging.info("Received SIGTERM: I will stop now")
		sys.exit(SIGTERM)

	signal(SIGTERM, handler)
	do_sleep(3)


def hard_to_kill():
	"""
	Ignores a SIGTERM and keeps running
	"""
	def ignore(*args):
		logging.info("Received SIGTERM: Not gunna die easily!! Ignoring.")

	signal(SIGTERM, ignore)
	do_sleep(2)


def cpu_affinity():
	"""

	"""
	if platform.system() not in ('Linux', 'FreeBSD'):
		cpus = tuple(xrange(psutil.NUM_CPUS))
		logging.warn('(Platform Unsupported; Showing all) cpu_affinity == %s', cpus)
		return

	aff = psutil.Process(os.getpid()).get_cpu_affinity()
	logging.info('cpu_affinity == %s', tuple(aff))
	spawn(1)


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
	p.wait()


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
)

def fail():
	print "Usage: %s <command>" % sys.argv[0]
	print [c.__name__ for c in COMMANDS]
	sys.exit(255)


if __name__ == "__main__":
	try:
		cmdName = sys.argv[1]
	except IndexError:
		fail()

	for fn in COMMANDS:
		if fn.__name__ == cmdName:
			logging.info("Running as UID,GID == %d,%d", os.getuid(), os.getgid())
			fn()
			sys.exit(0)

	# no valid commands passed
	fail() 




