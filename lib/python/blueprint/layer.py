import os
import logging

from collections import namedtuple

from job import Job
from io import Io, system
from app import PluginManager

logger = logging.getLogger(__name__)

LayerDepend = namedtuple("LayerDepend", ["dependent", "dependOn", "type"])

class LayerAspect(type):
    def __call__(cls, *args, **kwargs):
        """
        Intercepts the creation of layer objects and assigns
        them to the currnt job, of there is one.  The current job
        is set when a job is loaded via script.
        """
        layer = super(LayerAspect, cls).__call__(*args, **kwargs)
        if Job.Current:
            Job.Current.addLayer(layer)
        layer._afterInit()

        try:
            PluginManager.initLayer(layer)
        except Exception, e:
            logger.warn(e)

        return layer

class Layer(object):

    __metaclass__ = LayerAspect

    DependAll = "LayerDependAll"
    DependByTask = "LayerDependTask"

    def __init__(self, name, **args):
        self.__name = name
        self.__args = args
        self.__req_args = []
        self.__tasks = []
        self.__job = None
        self.__depends = []
        self.__outputs = { }
        self.__inputs = { }
        self.__setups = []

        self.__handleDependOnArg()

    def __handleDependOnArg(self):
        """
        Handles the dependOn kwarg passed in via the constructor.

        Dependencies can be specified in the layer constructor one
        of two ways.  First, by string identifier:

        foo = Layer("foo", dependOn=["bar", "bing:all"])

        Or by reference, using a tuple:

        Layer("zing", dependOn=[(foo, Layer.DependAll)])

        """
        for dep in self.__args.get("dependOn", list()):
            if isinstance(dep, (tuple, list)):
                self.dependOn(LayerDepend(self, dep[0], dep[1]))
            else:
                dtype = Layer.DependByTask
                onLayer = str(dep)
                if onLayer.endswith(":all"):
                    dtype = Layer.DependAll
                    onLayer = onLayer.split(":")[0]
                self.dependOn(onLayer, dtype) 

    def getName(self):
        return self.__name

    def setArg(self, name, value):
        self.__args[name] = value

    def getArg(self, name, default=None):
        return self.__args.get(name, default)

    def requireArg(self, name, types=None):
        self.__req_args.append((name, types))

    def execute(self, frames):
        self._execute(frames)

    def setup(self):
        self._setup()

    def getJob(self):
        return self.__job

    def setJob(self, job):
        self.__job = job

    def putData(self, name, data):
        self.__job.getArchive().putData(
            "%s/%s" % (self.__name, name), data)

    def dependOn(self, otherLayer, dtype=DependByTask):
        self.__depends.append(LayerDepend(self, otherLayer, dtype))

    def getDepends(self):
        return list(self.__depends)

    def getData(self, name):
        pass

    def putFile(self, name, file):
        pass

    def getFile(self, name):
        pass

    def getOutputs(self):
        return self.__outputs.values()

    def getInputs(self):
        return self.__inputs.values()

    def addInput(self, name, path, attrs=None):
        self.__inputs[name] = Io(name, path, attrs)

    def addOutput(self, name, path, attrs=None):
        self.__outputs[name] = Io(name, path, attrs)

    def getTempDir(self):
        return os.environ["TMPDIR"]

    def getSetupTasks(self):
        return list(self.__setups)

    def addSetupTask(self, task):
        self.__setups.append(task)

    def system(self, cmd):
        system(cmd)

    def _execute(self, frames):
        pass

    def _setup(self):
        pass

    def _afterInit(self):
        pass

    def _afterExecute(self):
        pass

class Task(Layer):

    def __init__(self, name, **args):
        Layer.__init__(self, name, **args)


class SetupTask(Task):
    
    def __init__(self, layer, **args):
        Task.__init__(self, "%s_setup" % layer.getName(), **args)
        self.__layer = layer
        self.__layer.dependOn(self, Layer.DependAll)

    def getLayer(self):
        return self.__layer



