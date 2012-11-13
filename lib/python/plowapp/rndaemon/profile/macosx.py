"""
System profile plugin for Mac OSX.  Should work on Tiger or higher.
"""
import os
import ctypes
import logging

from ctypes.util import find_library

from .posix import SystemProfiler as PosixSystemProfiler

logger = logging.getLogger(__name__)

__all__ = ["SystemProfiler"]

memstat = ctypes.cdll.LoadLibrary(
    os.path.join(os.path.dirname(__file__), "libmemstat.dylib"))


class sysinfo_t(ctypes.Structure):
    _fields_ = [
        ("totalRamMb", ctypes.c_uint64),
        ("freeRamMb", ctypes.c_uint64),
        ("totalSwapMb", ctypes.c_uint64),
        ("freeSwapMb", ctypes.c_uint64),
        ("physicalCpus", ctypes.c_uint32),
        ("logicalCpus", ctypes.c_uint32),
        ("bootTime", ctypes.c_uint64),
        ("cpuModel", ctypes.c_char_p)]


class SystemProfiler(PosixSystemProfiler):

    def __init__(self):
        super(SystemProfiler, self).__init__()

        self.hyperthread_factor = max(self.logicalCpus // self.physicalCpus, 1)

    def __repr__(self):
        return "<%s: OSX>" % self.__class__.__name__

    def _update(self):
        f = sysinfo_t()
        memstat.foo(ctypes.byref(f))
        self.data.update(dict([(field[0], 
            getattr(f, field[0])) for field in sysinfo_t._fields_]))

    def getSubprocessOpts(self, cmd, **kwargs):
        """
        getSubprocessOpts(list|str cmd, **kwargs) -> (cmd, dict)

        Method for returning the appropriate subprocess.Popen 
        arguments and kw arguments for an OSX platform. 

        """
        cmd, opts = super(SystemProfiler, self).getSubprocessOpts(cmd, **kwargs)

        env = opts['env']
        core_count = int(env['PLOW_CORES'])

        # only allow hyperthreads if the subprocess is requesting
        # all cpus on the machine, since we have no way to set cpu
        # affinity on OSX.
        if core_count >= self.physicalCpus:
            env['PLOW_THREADS'] = str(core_count * self.hyperthread_factor) 
        else:
            env['PLOW_THREADS'] = str(core_count)

        return cmd, opts
