"""
System profile plugin for Linux.
"""
import os
import re
import logging
import itertools
from functools import partial

import psutil

from .posix import SystemProfiler as PosixSystemProfiler

logger = logging.getLogger(__name__)

__all__ = ["SystemProfiler"]


class SystemProfiler(PosixSystemProfiler):

    def __init__(self):
        super(SystemProfiler, self).__init__()

        self.data['bootTime'] = int(psutil.BOOT_TIME)

        self.cpuprofile = None

        self._init_cpu_info()

    def __repr__(self):
        return "<%s: Linux>" % self.__class__.__name__

    def _init_cpu_info(self):
        """Init CPU stats that don't change over time"""

        cpuinfo = CpuProfile()

        model = ''

        if cpuinfo.physical_cpus:
            # grab the model of the first processor entry
            first_cpu = cpuinfo.physical_cpus.itervalues().next()
            model = first_cpu.get('model', '')
            self.hyperthread_factor = first_cpu.get('ht_factor', 1)

        self.data.update({
            'cpuModel': model,
            'physicalCpus': cpuinfo.num_phys_cpus,
            'logicalCpus': psutil.NUM_CPUS,
        })

        self.cpuprofile = cpuinfo

    def _update(self):
        memstats = psutil.virtual_memory()
        swapstats = psutil.swap_memory()

        b_to_mb = 1024 ** 2
        self.data.update({
            'freeRamMb': memstats.available / b_to_mb,
            'totalRamMb': memstats.total / b_to_mb,
            'freeSwapMb': swapstats.free / b_to_mb,
            'totalSwapMb': swapstats.total / b_to_mb,
        })

    def getSubprocessOpts(self, cmd, **kwargs):
        """
        getSubprocessOpts(list|str cmd, **kwargs) -> (cmd, dict)

        Method for returning the appropriate subprocess.Popen
        arguments and kw arguments for a Linux platform.

        """
        cmd, opts = super(SystemProfiler, self).getSubprocessOpts(cmd, **kwargs)

        cpuprofile = self.cpuprofile

        env = opts['env']

        uid = env.get('PLOW_TASK_UID')
        gid = env.get('PLOW_TASK_GID')
        cpus = kwargs.get('cpus', set())

        opts['preexec_fn'] = partial(self._preexec_fn, 
                                    uid, gid, cpus, 
                                    self.cpuprofile.logical_cpus)

        return cmd, opts

    @staticmethod
    def _preexec_fn(*args):
        """
        _preexec_fn(*args) -> void

        static method used for a subprocess.Popen call, 
        to be executed in the process right before calling the command.

        Sets the process to the given uid and gid. 
        Locks hyperthreaded processors to the process tree
        """
        uid, gid, cpus, cpu_map = args

        if not None in (uid, gid):
            os.setgid(int(gid))
            os.setuid(int(uid))

        logical_ids = set()
        for slot in cpus:
            logical_ids.update(cpu_map.get(slot, []))

        # logger = logging.getLogger(__name__)
        # logger.debug("Would lock logical processor ids %s", logical_ids)

        p = psutil.Process(os.getpid())
        p.set_cpu_affinity(logical_ids)


class CpuProfile(object):
    """
    CpuProfile 

    Class that represents the mappings between 
    physical cpus, cores per cpu, and the logical 
    processors in the system. 

    Helps to account for and group hyperthreaded 
    processors with their actual core id and cpu id.
    """

    CPUINFO = '/proc/cpuinfo'

    def __init__(self):
        self.physical_cpus = {}
        self.logical_cpus = {}
        self.num_cpus = 0
        self.num_phys_cpus = 0

        self.update()

    def update(self):
        FIELDS = set([
            'processor', 'physical id', 'siblings', 
            'cpu cores', 'core id', 'model name'
        ])

        cpus = {}
        proc = {}

        log_cpu_count = 0

        with open(self.CPUINFO) as f:
            for line in f:

                # if we reach a delimeter, save the current
                # proc object before clearing and starting over
                if proc and line.strip() == "":
                    phys_id = proc.get('physical id', -1)
                    core_id = proc.get('core id', log_cpu_count)
                    proc_id = proc['processor']
                    phys_dict = cpus.get(phys_id, {})

                    phys_dict.setdefault('processors', {})\
                                .setdefault(core_id, set()).add(proc_id)

                    if not phys_id in cpus:
                        sibs = proc.get('siblings', 1)
                        cores = proc.get('cpu cores', 1)
                        model = re.sub(r'\s+', ' ', proc['model name'])
                        ht_factor = (sibs / cores) if (sibs > cores) else 1

                        phys_dict['siblings'] = sibs
                        phys_dict['num_cores'] = cores
                        phys_dict['model'] = model
                        phys_dict['ht_factor'] = ht_factor
                        phys_dict['ht_enabled'] = True if ht_factor > 1 else False

                        cpus[phys_id] = phys_dict

                    proc = {}
                    log_cpu_count += 1

                    continue

                try: 
                    k, v = re.split(r'\s+:\s*', line, 1)
                except ValueError: 
                    continue

                if k in FIELDS:
                    v = v.strip()
                    proc[k.strip()] = int(v) if v.isdigit() else v

        # For vm's or systems that aren't reporting the real
        # physical cpu id's, fix the "catch-all" category count
        # to just represent all processors as physical
        phys_dict = cpus.get(-1)
        if phys_dict:
            total = len(phys_dict['processors'])
            phys_dict['siblings'] = total
            phys_dict['num_cores'] = total

        self.num_cpus = log_cpu_count
        self.num_phys_cpus = sum(i['num_cores'] for i in cpus.itervalues())
        self.physical_cpus = cpus 

        self.logical_cpus = dict(enumerate(itertools.chain.from_iterable(
            cpu['processors'].itervalues() for cpu in cpus.itervalues() 
        )))
