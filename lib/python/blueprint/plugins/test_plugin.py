
import logging

logger = logging.getLogger(__name__)

class Init:
    Layer = []
    Loaded = False
    AfterInit = False
    Setup = False
    BeforeExecute = False
    AfterExecute = False

def initLayer(layer):
    logger.info("initializing %s plugin on layer %s" % (__name__, layer))
    Init.Layer.append(layer)

def init():
    logger.info("init up %s plugin." %  __name__)
    Init.Loaded = True

def afterInit(layer):
    logger.info("afterInit %s plugin." %  __name__)
    Init.AfterInit = True

def setup(layer):
    logger.info("setup %s plugin." %  __name__)
    Init.Setup = True

def beforeExecute(layer):
    logger.info("beforeExecute %s plugin." %  __name__)
    Init.BeforeExecute = True

def afterExecute(layer):
    logger.info("afterExecute %s plugin." %  __name__)
    Init.AfterExecute = True