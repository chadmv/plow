"""The archive provides a mechanism for storing job runtime data."""

import os
import uuid
import logging
import yaml

import plow.conf as conf

from plow.blueprint.exception import ArchiveException

logger = logging.getLogger(__name__)

class Archive(object):
    """
    The archive provides a mechanism for storing job/layer runtime data.
    It can be used for passing arbitrary metadata between tasks as well
    as files.
    """
    def __init__(self, job):
        self.__job = job
        self.__path = os.path.join(
            conf.get("blueprint", "archive_path"), str(uuid.uuid4()))
        self.__make()

    def __make(self):
        logger.info(self.__path)
        os.makedirs(self.__path, 0777)
        os.mkdir(os.path.join(self.__path, "layers"), 0777)

    def put_data(self, name, data, layer=None):
        """Puts data into the archive."""
        path = os.path.join(self.get_path(layer), name);
        fp = open(path, "w")
        try:
            fp.write(yaml.dump(data))
        finally:
            fp.close()

    def get_data(self, name):
        pass

    def put_file(self, name, path):
        pass

    def get_file(self, name):
        pass

    def get_path(self, layer=None):
        if layer:
            try:
                layer_name = layer.get_name()
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
