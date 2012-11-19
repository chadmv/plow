import os
import logging
import tempfile

from collections import namedtuple

from job import Job
from io import Io, system
from app import PluginManager

logger = logging.getLogger(__name__)

Depend = namedtuple("Depend", ["dependent", "dependOn", "type", "args"])

class DependType(object):
    All = "DependAll"
    ByTask = "ByTask"

class LayerAspect(type):

    def __call__(cls, *args, **kwargs):
        """
        Intercepts the creation of layer objects and assigns
        them to the current job, of there is one.  The current job
        is set when a job is loaded via script.
        """
        layer = super(LayerAspect, cls).__call__(*args, **kwargs)

        if Job.Current:
            Job.Current.addLayer(layer)
        
        layer.afterInit()
        return layer


class Layer(object):
    """
    A base class which implments the core functionality of
    an executable entity.
    """
    __metaclass__ = LayerAspect

    def __init__(self, name, **args):
        self.__name = name
        self.__args = args

        self.__job = None
        self.__req_args = []
        self.__depends = []
        self.__setups = []
        self.__outputs = {}
        self.__inputs = {}

        self.__handleDependArg()

    def getName(self):
        return self.__name

    def setArg(self, name, value):
        self.__args[name] = value

    def getArg(self, name, default=None):
        return self.__args.get(name, default)

    def isArgSet(self, name):
        return self.__args.has_key(name)

    def requireArg(self, name, types=None):
        self.__req_args.append((name, types))

    def dependOn(self, other, args=None):
        self.__depends.append(Depend(self, other, DependType.ByTask, args))

    def dependAll(self, other, args=None):
        self.__depends.append(Depend(self, other, DependType.All, args))

    def getDepends(self):
        return list(self.__depends)

    def getJob(self):
        return self.__job

    def putData(self, name, data):
        self.__job.getArchive().putData(
            "%s/%s" % (self.__name, name), data)

    def getData(self, name):
        self.__job.getArchive().getData(
            "%s/%s" % (self.__name, name))

    def setJob(self, job):
        self.__job = job

    def getOutput(self, name):
        return self.__outputs[name]

    def getInput(self, name):
        return self.__inputs[name]

    def getOutputs(self):
        return self.__outputs.values()

    def getInputs(self):
        return self.__inputs.values()

    def addInput(self, name, path, attrs=None):
        self.__inputs[name] = Io(name, path, attrs)

    def addOutput(self, name, path, attrs=None):
        self.__outputs[name] = Io(name, path, attrs)

    def getSetupTasks(self):
        return list(self.__setups)

    def addSetupTask(self, task):
        self.__setups.append(task)

    def system(self, cmd):
        system(cmd)

    def afterInit(self):
        self._afterInit()
        PluginManager.runAfterInit(self)

    def setup(self):
        self._setup()
        PluginManager.runSetup(self)

    def beforeExecute(self):
        self._beforeExecute()
        PluginManager.runBeforeExecute(self)

    def execute(self, *args):
        self.beforeExecute()
        self._execute(*args)
        self.afterExecute()

    def afterExecute(self):
        self._afterExecute()
        PluginManager.runAfterExecute(self)

    def getTempDir(self):
        return tempfile.gettempdir()
    
    def getDir(self):
        return self.__job.getArchive().getPath(self.getName())

    def system(self, cmd):
        system(cmd)

    def _afterInit(self):
        pass

    def _setup(self):
        pass

    def _beforeExecute(self):
        pass

    def _execute(self):
        pass

    def _afterExecute(self):
        pass
    
    def __handleDependArg(self):
        """
        Handles the dependOn kwarg passed in via the constructor.

        foo = Layer("foo", dependOn=["bar", "bing:all"])
        Layer("zing", dependOn=[(foo, Layer.DependAll)])
        """
        if not self.isArgSet("depend"):
            return

        depends = self.getArg("depend")
        if not isinstance(depends, (list,tuple)):
            depends = [depends]

        for dep in depends:
            if isinstance(dep, (tuple, list)):
                self.dependOn(LayerDepend(self, dep[0], dep[1]))
            else:
                onLayer = str(dep)
                if onLayer.endswith(":all"):
                    self.dependAll(onLayer.split(":")[0])
                else:
                    self.dependOn(onLayer)

    def __str__(self):
        return self.getName()

class Task(Layer):
    """
    Tasks are indiviudal processes with no-frame range.  Tasks must be parented
    to a layer.
    """
    def __init__(self, name, **args):
        Layer.__init__(self, name, **args)
   
class SetupTask(Task):

    def __init__(self, layer, **args):
        Task.__init__(self, "%s_setup" % layer.getName(), **args)
        self.__layer = layer 
        layer.dependOn(self, DependType.All)

        self.setArg("group", "setups")

    def getLayer(self):
        return self.__layer



