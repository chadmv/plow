
import os
import argparse
import logging
import yaml

from plow.blueprint.plowrun import BlueprintRunner
from plow.blueprint.job import Job

def get_standard_arg_parser(descr):
    argparser = argparse.ArgumentParser(description=descr)
    group = argparser.add_argument_group("Plow Options")
    group.add_argument("-server", metavar="HOSTNAME", help="Specify the Plow server node communicate with.")
    group.add_argument("-verbose", action="store_true", help="Turn on verbose logging.")
    group.add_argument("-debug", action="store_true", help="Turn on debug logging.")
    return argparser

def get_blueprint_arg_parser(descr):
    argparser = get_standard_arg_parser(descr)
    group = argparser.add_argument_group("Blueprint Options")
    group.add_argument("script", metavar="SCRIPT", help="Path to blueprint script.")
    group.add_argument("range", metavar="FRAME_RANGE", help="Frame range.")
    group.add_argument("-pause", action="store_true", help="Launch the job in a paused state.")
    group.add_argument("-job-name", metavar="JOB NAME", help="Set the job name.")
    return argparser

def load_script(path):

    if os.path.basename(path) == "blueprint.yaml":
        Job.Current = yaml.load(file(path, 'r'))
        # Yamlized jobs have no session but they
        # have a path that points to one so we have
        # to bring back the archive.
        if job.get_path():
            job.load_archive()
    else:
        Job.Current = Job(os.path.basename(path).split(".")[0])
        execfile(path, {})

    return Job.Current

class PlowApplication(object):
    def __init__(self, descr):
        self._argparser = get_standard_arg_parser(descr) 
    
    def handle_args(self):
        pass

class BlueprintApplication(object):

    def __init__(self, descr):
        self._argparser = get_blueprint_arg_parser(descr)
        self._args = None

    def handle_args(self, args):
        pass

    def go(self):
        self._runner = BlueprintRunner()
        args = self._argparser.parse_args()
        print args

        if args.verbose:
            logging.basicConfig(level=logging.INFO)
        if args.debug:
            logging.basicConfig(level=logging.DEBUG)
        if args.pause:
            self._runner.set_arg("pause", args.pause)
        if args.job_name:
            self._runner.set_arg("job_name", args.job_name)
        self._runner.set_arg("range", args.range)

        # Handle arguments added by specific application.
        self.handle_args(args)

        job = load_script(args.script)
        job.setup()

        if len(job.get_layers()) == 0:
            raise Exception("The job has no layers to execute.");
        self._runner.run(job)








