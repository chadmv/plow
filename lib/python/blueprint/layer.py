
from job import Job

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
        return layer

class Layer(object):

    __metaclass__ = LayerAspect

    def __init__(self, name, **args):
        self.__name = name
        self.__args = args
        self.__req_args = []
        self.__tasks = []
        self.__job = None

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

    def getData(self, name):
        pass

    def putFile(self, name, file):
        pass

    def getFile(self, name):
        pass

    def _execute(self, frames):
        pass

    def _setup(self):
        pass


