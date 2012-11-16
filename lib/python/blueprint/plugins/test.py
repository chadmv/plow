
import logging

logger = logging.getLogger(__name__)

class Init:
    Layer = []
    Job = []
    Setup = False

def initLayer(layer):
    logger.info("initializing %s plugin on layer %s" % (__name__, layer))
    Init.Layer.append(layer)

def initJob(job):
    logger.info("initializing %s plugin on job %s" % __name__)
    Init.Job.append(job)

def setup():
    logger.info("setting up %s plugin." %  __name__)
    Init.Setup = True