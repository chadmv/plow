
import os
import argparse
import logging
import yaml

from plow.blueprint.plowrun import BlueprintRunner
from plow.blueprint.job import Job

class BlueprintApplication(object):
    def __init__(self, descr):
        self._argparser = argparse.ArgumentParser(description=descr)
        group = self._argparser.add_argument_group("Plow Options")
        group.add_argument("-server", metavar="HOSTNAME", help="Specify the Plow server node communicate with.")
        group.add_argument("-verbose", action="store_true", help="Turn on verbose logging.")
        group.add_argument("-debug", action="store_true", help="Turn on debug logging.")

    def handleArgs(self, args):
        pass

    def go(self):
        args = self._argparser.parse_args()

        if args.verbose:
            logging.basicConfig(level=logging.INFO)
        if args.debug:
            logging.basicConfig(level=logging.DEBUG)
  
        # Handle arguments added by specific application.
        self.handleArgs(args)

def loadScript(path):

    if os.path.basename(path) == "blueprint.yaml":
        Job.Current = yaml.load(file(path, 'r'))
        # Yamlized jobs have no session but they
        # have a path that points to one so we have
        # to bring back the archive.
        if Job.Current.getPath():
            Job.Current.loadArchive()
    else:
        Job.Current = Job(os.path.basename(path).split(".")[0])
        execfile(path, {})

    return Job.Current






