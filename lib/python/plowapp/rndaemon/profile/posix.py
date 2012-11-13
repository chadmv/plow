"""
System profile plugin for POSIX Platforms.
"""
import logging
import subprocess
import os
import shlex
import pwd
import tempfile
from functools import partial

from .base import AbstractProfiler

from ..rpc import ttypes
from .. import conf

logger = logging.getLogger(__name__)

__all__ = ["SystemProfiler"]


class SystemProfiler(AbstractProfiler):

    def reboot(self):
        """
        reboot()

        Platform-specific procedure to reboot the system. 
        Should be implemented in a subclass if platform is not POSIX 
        """
        p = subprocess.Popen(['/usr/bin/sudo', '-n', '/sbin/reboot'], 
                    stdout=subprocess.PIPE, stderr=subprocess.STDOUT)

        out, _ = p.communicate()

        # if we have gotten here, then the reboot obviously failed
        err = "System failed to reboot (status %d): %s" % (p.returncode, out.strip())
        logger.warn(err)
        raise ttypes.RndException(p.returncode, err)     

    def getSubprocessOpts(self, cmd, **kwargs):
        """
        getSubprocessOpts(list|str cmd, **kwargs) -> (cmd, dict)

        Method for returning the appropriate subprocess.Popen 
        arguments and kw arguments for a POSIX platform. 

        """
        cmd, opts = super(SystemProfiler, self).getSubprocessOpts(cmd, **kwargs)

        if isinstance(cmd, (str, unicode)):
            cmd = shlex.split(cmd)

        uid = kwargs.get('uid')

        if os.geteuid() == 0:
            gid = None
            p_struct = None 

            if conf.TASK_PROXY_USER:
                try:
                    p_struct = pwd.getpwnam(conf.TASK_PROXY_USER)
                except KeyError:
                    logger.warn("User '%s' not found. Not changing for task", conf.TASK_PROXY_USER)
                else:
                    uid, gid = p_struct.pw_uid, p_struct.pw_gid

            if uid is None:
                return cmd, opts 

            if p_struct is None or not conf.TASK_PROXY_USER:
                p_struct = pwd.getpwuid(uid)
                gid = p_struct.pw_gid

            username = p_struct.pw_name
            homedir = p_struct.pw_dir

            if not os.path.exists(homedir):
                homedir = tempfile.gettempdir()

            opts['cwd'] = homedir

            opts['env'].update({
                'USERNAME': username,
                'LOGNAME': username,
                'USER': username,
                'PWD': homedir,
                'HOME': homedir,

                'PLOW_TASK_UID': str(uid),
                'PLOW_TASK_GID': str(gid),
            })

            logger.debug("Switching user to (%d, %d)", uid, gid)
            opts['preexec_fn'] = partial(self._preexec_fn, uid, gid)

        return cmd, opts  

    @staticmethod
    def _preexec_fn(*args):
        """
        _preexec_fn(*args) -> void

        function used for a subprocess.Popen call, 
        to be executed in the process right before calling the command.

        Sets the process to the given uid and gid. 
        """
        uid, gid = args
        os.setgid(gid)
        os.setuid(uid)
