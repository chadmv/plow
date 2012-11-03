
import os
import argparse
import logging
import yaml

import conf

logger = logging.getLogger(__name__)

__all__ = [
    "Application",
    "BlueprintRunner",
    "loadBackendPlugin",
    "loadScript"]

class Application(object):
    def __init__(self, descr):
        self._argparser = argparse.ArgumentParser(description=descr)
        group = self._argparser.add_argument_group("Logging Options")
        group.add_argument("-verbose", action="store_true",
            help="Turn on verbose logging.")
        group.add_argument("-debug", action="store_true", 
            help="Turn on debug logging.")

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

class BlueprintRunner(object):

    def __init__(self, **kwargs):
        self.__args = {
            "paused": False
        }
        self.__args.update(kwargs)
        self.__job = None


    def setArg(self, key, value):
        self.__args[key] = value

    def getArg(self, key, default=None):
        return self.__args.get(key, default)

    def launch(self):
        # Load the backend module
        print conf.get("defaults", "backend")
        backend = loadBackendPlugin(self.getArg("backend",
            conf.get("defaults", "backend")))
        backend.launch(self)

    def setup(self):
        return self.getJob().setup()

    def getJob(self):
        if not self.__job:
            self.__job = loadScript(self.getArg("script"))
        return self.__job


def loadBackendPlugin(name):
    logger.debug("loading queue backend: %s" % name)
    return __import__("blueprint.backend.%s" % name, globals(), locals(), [name])

def loadScript(path):

    from blueprint.job import Job
    
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






