import logging
import subprocess

logger = logging.getLogger(__name__)

def system(cmd, frames=None):
    logger.info("About to run: %s" % " ".join(cmd))
    p = subprocess.Popen(cmd, shell=False)
    p.wait()

class Io(object):
    def __init__(self, name, path, attrs=None):
        self.name = name
        self.path = path
        self.attrs = attrs or dict()

    def __str__(self):
        return "<IO %s %s>" % (self.name, self.path)


