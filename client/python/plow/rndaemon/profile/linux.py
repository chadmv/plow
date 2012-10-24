"""
System profile plugin for Linux.
"""
import os
import re
import logging

import psutil

from base import AbstractProfiler

logger = logging.getLogger(__name__)

__all__ = ["SystemProfiler"]

class SystemProfiler(AbstractProfiler):

    def __init__(self):
        AbstractProfiler.__init__(self)

        self.data['bootTime'] = int(psutil.BOOT_TIME)

        self._init_cpu_info()

    def __repr__(self):
        return "<%s: Linux>" % self.__class__.__name__

    def _init_cpu_info(self):
        """Init CPU stats that don't change over time"""
        cores = 1
        cpus_set = set()
        model = ''

        with open('/proc/cpuinfo') as f:
            for line in f:
                try:
                    k,v = re.split(r'\s*:\s*', line, 1)
                except ValueError:
                    continue
                v = v.strip()
                if k == 'cpu cores':
                    cores = int(v)
                elif k == 'physical id':
                    cpus_set.add(v)
                elif k == 'model name':
                    model = v

        phys_cores = (cores or 1) * (len(cpus_set) or 1)

        self.data.update({
            'cpuModel'      : model,
            'physicalCpus'  : phys_cores,
            'logicalCpus'   : psutil.NUM_CPUS,
        })

    def _update(self):
        memstats = psutil.virtual_memory()
        swapstats = psutil.swap_memory()

        b_to_mb = 1024**2
        self.data.update({
            'freeRamMb'     : memstats.available / b_to_mb,
            'totalRamMb'    : memstats.total / b_to_mb,
            'freeSwapMb'    : swapstats.free / b_to_mb,
            'totalSwapMb'   : swapstats.total / b_to_mb,
        })        


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    s = SystemProfiler()



