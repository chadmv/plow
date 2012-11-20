import logging
import subprocess

from exception import CommandException

logger = logging.getLogger(__name__)

def system(cmd, frames=None):
    cmdStr = " ".join(cmd)
    logger.info("About to run: %s", cmdStr)
    p = subprocess.Popen(cmd, shell=False)
    ret = p.wait()

    if ret != 0:
        raise CommandException(
            'Command exited with a status of %d: "%s"' % (ret, cmdStr),
            exitStatus=ret
        )

class Io(object):
    def __init__(self, name, path, attrs=None):
        self.name = name
        self.path = path
        self.attrs = attrs or dict()

    def __str__(self):
        return "<IO %s %s %s>" % (self.name, self.path, self.attrs)

    def __repr__(self):
        return "<IO %s %s %s>" % (self.name, self.path, self.attrs)
