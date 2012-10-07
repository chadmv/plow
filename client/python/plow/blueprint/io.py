import logging
import subprocess

logger = logging.getLogger(__name__)

def shell_out(cmd, frames=None):
    logger.info("About to run: %s" % " ".join(cmd))
    subprocess.call(cmd, shell=False)
