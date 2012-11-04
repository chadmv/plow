"""The archive provides a mechanism for storing job runtime data."""

import os
import uuid
import logging
import yaml

import conf
from exception import ArchiveException

logger = logging.getLogger(__name__)

class Archive(object):
    """
    The archive provides a mechanism for storing job/layer runtime data.
    It can be used for passing arbitrary metadata between tasks as well
    as files.
    """
    def __init__(self, job):
        self.__job = job
        self.__path = "-".join(
            (conf.get("templates", "archive_dir", JOB_NAME=job.getName()),
            "-%s" % uuid.uuid4()))
        self.__make()

    def __make(self):
        os.makedirs(self.__path, 0777)
        os.mkdir(os.path.join(self.__path, "layers"), 0777)

    def putData(self, name, data, layer=None):
        """Puts data into the archive."""
        path = os.path.join(self.getPath(layer), name);
        fp = open(path, "w")
        try:
            fp.write(yaml.dump(data))
        finally:
            fp.close()

    def getData(self, name):
        pass

    def putFile(self, name, path):
        pass

    def getFile(self, name):
        pass

    def getPath(self, layer=None):
        if layer:
            try:
                layer_name = layer.getName()
            except:
                layer_name = str(layer)
            path = os.path.join(self.__path, "layers", layer_name)
        else:
            path = self.__path

        if not os.path.exists(path):
            try:
                os.mkdir(path, 0777)
            except OSError, e:
                raise ArchiveException("Failed to make archive dir: " + e)

        return path
