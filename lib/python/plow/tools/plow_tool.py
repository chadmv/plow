"""
plow-tool is a general purpose query tool for the Plow render farm.
"""
import logging
import argparse

import plow
import constants
import util

logger = logging.getLogger(__name__)

def main():
    
    parser = argparse.ArgumentParser(description="plow-tool - A general purpose query tool for the Plow render farm.")
    group0 = parser.add_argument_group("Core Options")
    group0.add_argument("-debug", action="store_true", help="Turn on debug logging.")
    
    group1 = parser.add_argument_group("Job Query Options")
    group1.add_argument("-ljn", nargs="?", const="", metavar="SUBSTR", help="List job names with optional name substr filters")
    group1.add_argument("-lj", nargs="?", const="", metavar="SUBSTR", help="List jobs with optional name substr filters")
    group1.add_argument("-all", action='store_true', help="Include finished jobs.")

    args = parser.parse_args()
    if args.debug:
        logging.basicConfig(level=logging.DEBUG)

    logger.debug(args)

    if isinstance(args.ljn, str):
        display_job_names(regex=args.ljn, finished=args.all)
    elif isinstance(args.lj, str):
        display_jobs(regex=args.lj, finished=args.all)
    else:
        parser.print_help()

def build_job_query(**kwargs):
    q = {
        "states": [plow.JobState.RUNNING],
        "regex": kwargs.get("regex")
    }
    if kwargs.get("finished"):
        q["states"].append(plow.JobState.Finished)
    logger.debug("Job query: %s" % q)
    return q

def display_job_names(**kwargs):
    q = build_job_query(**kwargs)
    for job in plow.client.get_jobs(**q):
        print job.name

def display_jobs(**kwargs):
    q = build_job_query(**kwargs)

    header = "%-30s %16s %16s  %4s  [%2s %2s %2s]  [%4s  ]  %8s  %-20s  %s"
    format = "%-30s %16s %16s  %04d  [%03d %03d %03d]  [%04dMB]  %8s  %-20s  %s"

    print header % (
        "Job",
        "State",
        "User",
        "Pend",
        "Run",
        "Min",
        "Max",
        "RAM",
        "Duration",
        "Start Time",
        "End Time"
    )
    for job in plow.client.get_jobs(**q):
        print format % (
            job.name, 
            constants.JOB_STATES[job.state],
            job.username,
            job.totals.waiting + job.totals.depend,
            job.runCores,
            job.minCores,
            job.maxCores,
            job.stats.highRam,
            util.formatDuration(job.startTime, job.stopTime),
            util.formatDateTime(job.startTime),
            util.formatDateTime(job.stopTime)
        )
