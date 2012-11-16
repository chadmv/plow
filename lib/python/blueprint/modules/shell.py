
import logging

from blueprint.layer import Layer
from blueprint.io import system

logger = logging.getLogger(__name__)

class Shell(Layer):

    def __init__(self, name, **args):
        """
        Executes a simple shell command over a given frame range.
        """
        Layer.__init__(self, name, **args)
        self.requireArg("cmd", (list, tuple))

    def _execute(self, frames):
        for frame in frames:
            system(self.getArg("cmd"))







