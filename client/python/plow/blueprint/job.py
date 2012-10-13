
import weakref

from archive import Archive
from exception import LayerException

class Job(object):

    Current = None

    def __init__(self, name):
        self.__name = name
        self.__layers = [ [], {} ]
        self.__archive = None
        self.__path = None

    def get_path(self):
        return self.__path

    def get_name(self):
        return self.__name

    def get_layer(self, name):
        try:
            return self.__layers[1][name]
        except KeyError:
            raise LayerException("Layer %s does not exist." % name)

    def add_layer(self, layer):
        
        if self.__layers[1].has_key(layer.get_name()):
            raise LayerException("Invalid layer name: %s , duplicate name." % layer.get_name())
        
        self.__layers[0].append(layer)
        self.__layers[1][layer.get_name()] = layer
        layer.set_job(self)

    def get_layers(self):
        return self.__layers[0]

    def load_archive(self):
        self.__archive = Archive(self)

    def setup(self):

        self.__archive = Archive(self)
        self.__path = self.__archive.get_path()

        for layer in self.__layers[0]:
            layer.setup()

        archive = self.__archive
        try:
            self.__archive = None
            archive.put_data("blueprint.yaml", self)
        finally:
            self.__archive = archive

