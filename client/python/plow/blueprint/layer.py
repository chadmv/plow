
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
            Job.Current.add_layer(layer)
        return layer

class Layer(object):

    __metaclass__ = LayerAspect

    def __init__(self, name, **args):
        self.__name = name
        self.__args = args
        self.__req_args = []
        self.__tasks = []
        self.__job = None

    def get_name(self):
        return self.__name

    def set_arg(self, name, value):
        self.__args[name] = value

    def get_arg(self, name, default=None):
        return self.__args.get(name, default)

    def require_arg(self, name, types=None):
        self.__req_args.append((name, types))

    def execute(self, frames):
        self._execute(frames)

    def setup(self):
        self._setup()

    def get_job(self):
        return self.__job

    def set_job(self, job):
        return self.__job

    def put_data(self, name, data):
        self.__job.get_session().put_data(
            "%s/%s" % (self.__name, name), data)

    def get_data(self, name):
        pass

    def put_file(self, name, file):
        pass

    def get_file(self, name):
        pass

    def _execute(self, frames):
        pass

    def _setup(self):
        pass


