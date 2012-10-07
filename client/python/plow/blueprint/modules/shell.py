
import logging

from plow.blueprint.layer import Layer
from plow.blueprint.io import shell_out

logger = logging.getLogger(__name__)

class Shell(Layer):

    def __init__(self, name, **args):
        """
        Executes a simple shell command over a given frame range.
        """
        Layer.__init__(self, name, **args)
        self.require_arg("cmd", (list, tuple))

    def _execute(self, frames):
        for frame in frames:
            shell_out(self.get_arg("cmd"))







