
import threading
import subprocess
import logging
import time
import os
import traceback
import errno

from collections import namedtuple, deque
from itertools import chain 

import psutil

import conf
import client
import utils
import rpc.ttypes as ttypes

from profile import SystemProfiler as _SystemProfiler

logger = logging.getLogger(__name__)


__all__ = ['Profiler', 'ResourceMgr', 'ProcessMgr']


_RunningProc = namedtuple("RunningProc", "processCmd pthread cpus")


#
# _ResourceManager
#
class _ResourceManager(object):
    """
    The ResourceManager keeps track of the bookable resources on the
    machine.  This is currently just cores, but memory and GPUS
    in the future.
    """

    def __init__(self):
        self.__slots = deque(xrange(Profiler.physicalCpus))
        self.__slots_all = tuple(self.__slots)
        self.__lock = threading.RLock()

        logger.info("Intializing resource manager with %d physical cores.", 
                    Profiler.physicalCpus)

    def checkout(self, numCores):
        if numCores < 1:
            raise ttypes.RndException(1, "Cannot reserve 0 slots")

        result = []

        with self.__lock:
            open_slots = self.__slots
            logger.info("Open slots: %s", list(open_slots))

            if numCores > len(open_slots):
                raise ttypes.RndException(1, "No more open slots")

            result = [open_slots.pop() for _ in xrange(numCores)]
    
        logger.info("Checked out CPUS: %s", result)
        return result

    def checkin(self, cores):
        with self.__lock:
            self.__slots.extend(cores)
            avail, total = len(self.__slots), Profiler.physicalCpus
        logger.info("Checked in CPUS: %s; Now available: %d / %d", cores, avail, total)

    def getSlots(self):
        return list(xrange(Profiler.physicalCpus))

    def getOpenSlots(self):
        with self.__lock:
            return list(self.__slots)


#
# _ProcessManager
#
class _ProcessManager(object):
    """
    The ProcessManager keeps track of the running tasks.  Each task
    is executed in a separate ProcessThread.
    """

    SAMPLE_INTERVAL_SEC = 10

    def __init__(self):
        self.__threads = {}
        self.__lock = threading.RLock()
        self.__timer = None 
        self.__isReboot = threading.Event()
        self.__isShutdown = threading.Event()

        self.__sampler = threading.Thread(target=self._processSampler)
        self.__sampler.daemon = True 
        self.__sampler.start()

        self.sendPing(True)

    @property 
    def isReboot(self):
        return self.__isReboot.is_set()

    def runProcess(self, processCmd, wait=-1):
        """
        Takes a RunTaskCommand object, reserves resources, 
        and starts the process. Default mode is to return None

        Optionally, a wait time may be specified in float
        seconds, to wait until the job has fully started, 
        before returning. If wait > -1, return a RunningTask object
        """
        cpus = ResourceMgr.checkout(processCmd.cores)
        pthread = _ProcessThread(processCmd, cpus)

        with self.__lock:
            self.__threads[processCmd.procId] = _RunningProc(processCmd, pthread, cpus)

        pthread.start()
        logger.info("process thread started")

        if wait == -1:
            return 

        task = pthread.getRunningTask(wait)

        return task

    def processFinished(self, processResult, cpus=None):
        """
        Callback for when a process has finished running. 
        Receives the RunTaskResult object. 
        Deallocates the resources.
        """
        with self.__lock:
            if cpus is None:
                cpus = self.__threads[processResult.procId].cpus
            ResourceMgr.checkin(cpus)
            try:
                del self.__threads[processResult.procId]
            except Exception, e:
                logger.warn("Process %s not found: %s", processResult.procId, e)

    def sendPing(self, isReboot=False, repeat=True):
        """
        Ping into the server with current task and resource states.
        If repeat is True, schedules another ping at an interval defined 
        by the rndaemon config.
        """
        if self.__isShutdown.is_set():
            repeat = False

        # TODO: What is the purpose of the isReboot flag?
        # Using the internal flag to determine if we are in a 
        # reboot state.
        isReboot = self.__isReboot.is_set()

        tasks = self.getRunningTasks()
        Profiler.sendPing(tasks, isReboot)

        # TODO: Maybe there needs to be a seperate thread for this check
        # but for now it is part of the ping loop.
        if isReboot and not tasks:
            logger.info("Task queue is empty and daemon is scheduled for reboot")
            try:
                Profiler.reboot()
            except ttypes.RndException, e:
                # on next loop, the server will see that the system
                # is no longer in isReboot state
                logger.warn(e.why)
                self.__isReboot.clear()
            else:
                # just in case
                return

        if repeat:
            self.__timer = threading.Timer(conf.NETWORK_PING_INTERVAL, self.sendPing)
            self.__timer.daemon = True
            self.__timer.start()

    def killRunningTask(self, procId, reason):
        """
        Kill a currently running task by its procId. 
        """
        logger.info("kill requested for procId %s, %s", procId, reason)

        with self.__lock:
            try:
                pthread = self.__threads[procId].pthread
            except KeyError:
                err = "Process %s not found" % procId
                logger.warn(err)
                # TODO: Raise a proper exception type? or
                # fail quietly?
                raise ttypes.RndException(1, err)

        _, not_killed = pthread.killProcess(reason=reason)

        if not_killed:
            err = "Failed to kill the following pids for prodId %s: %s" % \
                    (procId, ','.join(not_killed))
            logger.warn(err)
            raise ttypes.RndException(1, err)

    def getRunningTasks(self):
        """ Get a list of all running task objects """
        with self.__lock:
            tasks = [t.pthread.getRunningTask() for t in self.__threads.itervalues()]

        return tasks

    def shutdown(self):
        """
        Gracefully shut down all running tasks so they can report back in
        """
        logger.debug("Shutdown requested for process manager.")
        self.__isShutdown.set()

        with self.__lock:
            threads = [proc.pthread for proc in self.__threads.itervalues()]

        for t in threads:
            t.shutdown()

        logger.debug("Asked %d tasks to quit and report. Waiting for them to complete", len(threads))

        for t in threads:
            if not t.wait(10):
                logger.warn("Thread failed to close down after waiting 10 seconds: %r", t) 

        self.__threads.clear()
        del threads
        logger.debug("Done waiting on task shutdown")

    def reboot(self, now=False):
        """
        reboot (bool now=False)

        Reboot the system as soon as it becomes idle. That is, 
        when no tasks are running. 

        If now == True, reboot immediately, regardless of any 
        in-progress render tasks. 
        """
        # TODO: For now, assuming that even if they aren't root,
        # that they may have permission to reboot. This means a
        # reboot(now=False) will not raise an exception to the caller.
        #
        # if os.geteuid() != 0:
        #     err = "rndaemon not running as user with permission to reboot system"
        #     raise ttypes.RndException(1, err)

        self.__isReboot.set() 

        if now:
            logger.info("*SYSTEM GOING DOWN FOR IMMEDIATE REBOOT*")

            # stop all of the tasks
            self.shutdown()

            with self.__lock:
                if self.__timer:
                    self.__timer.cancel()
                # The reboot could happen from the ping if the task
                # queue is empty. 
                self.sendPing(repeat=False)

            # Otherwise, the reboot will happen here, regardless
            # of whether there are active tasks running.
            Profiler.reboot()

        else:
            logger.info("*Reboot scheduled at next idle event*")

    def _processSampler(self):
        """
        Loop that updates metrics on every running process 
        at intervals.
        """
        while not self.__isShutdown.is_set():
            with self.__lock:
                pthreads = [t.pthread for t in self.__threads.itervalues()]

            for pthread in pthreads:
                pthread.updateMetrics()

            time.sleep(self.SAMPLE_INTERVAL_SEC)


#
# RunningTask
#
class RunningTask(ttypes.RunningTask):
    """
    Subclass of ttypes.RunningTask that adjusts the
    __repr__ to only print a reduces amount of the last
    log line string.
    """
    def __repr__(self):
        D = self.__dict__.copy()

        # elide the log string if its too big
        lastLog = D.get('lastLog')
        if lastLog and len(lastLog) > 50:
            D['lastLog'] = '%s...' % lastLog[:47]

        L = ('%s=%r' % (key, value) for key, value in D.iteritems())
        return '%s(%s)' % (self.__class__.__name__, ', '.join(L))        


#
# _ProcessThread
#
class _ProcessThread(threading.Thread):
    """
    The _ProcessThread wraps a running task.
    """

    _DO_DISK_IO = hasattr(psutil.Process, "get_io_counters")

    def __init__(self, rtc, cpus=None):
        threading.Thread.__init__(self)
        self.daemon = True

        self.__logfp = None
        self.__cpus = cpus or set()

        self.__rtc = rtc
        self.__pptr = None
        self.__logfp = None
        self.__pid = -1

        self.__killThread = None

        self.__wasKilled = threading.Event()
        self.__hasStarted = threading.Event()
        self.__isShutdown = threading.Event()

        self.__progress = 0.0
        self.__lastLog = ""

        self.__killReason = ""

        self.__metrics = {
            'rssMb': 0,
            'maxRssMb': 0,
            'cpuPercent': 0,
            'diskIO': ttypes.DiskIO(-1,-1,-1,-1),
        }

    def __repr__(self):
        return "<%s: (procId: %s, pid: %d)>" % (
            self.__class__.__name__, 
            self.__rtc.procId, 
            self.__pid)

    def shutdown(self):
        """
        Instruct the process to shutdown gracefully.
        Returns the same output as killProcess()
        """
        logger.debug("Shutdown request received. Killing %r", self)
        self.__isShutdown.set()
        self.killProcess(block=False, reason="rndaemon shutdown request received")

    def wait(self, timeout=None):
        """
        Waits for the process to finish. 
        By default, blocks indefinitely. Specify a
        timeout in float seconds to wait. If the timeout 
        value is exceeded, return False
        Returns True if the task ended. 
        """
        self.join(timeout)
        return not self.isAlive()

    def getRunningTask(self, wait=-1):
        """
        getRunningTask(float wait=-1) -> RunningTask

        Returns a RunningTask instance representing 
        the current state of the task.

        If wait > 0, then wait that many seconds for
        the process to start. This is useful if you are
        creating the process and then checking its running
        task right away. Some information may not be 
        available until after the thread has gotten the
        process running.
        """
        if wait > 0:
            self.__hasStarted.wait(wait)

        rt = RunningTask()
        rtc = self.__rtc

        rt.jobId = rtc.jobId
        rt.procId = rtc.procId
        rt.taskId = rtc.taskId
        rt.layerId = rtc.layerId
        rt.pid = self.__pid

        metrics = self.__metrics

        rt.rssMb = metrics['rssMb']
        rt.cpuPercent = metrics['cpuPercent']

        if self._DO_DISK_IO:
            rt.diskIO = metrics['diskIO']

        rt.progress = self.__progress 
        rt.lastLog = self.__lastLog or None 

        return rt

    def run(self):
        """
        Run method called implicitely by start() 
        Fires up the process to do the actual task. 
        Logs output, and records resource metrics.
        """
        rtc = self.__rtc 
        retcode = 1

        try:
            uid = self.__rtc.uid
            cpus = self.__cpus 
            
            logger.info("Opening log file: %s", rtc.logFile)
            self.__logfp = utils.ProcessLog(self.__rtc.logFile, uid=uid, buffering=1)
            self.__logfp.writeLogHeader(rtc)

            env = os.environ.copy()
            env.update(rtc.env)

            parser = None
            if rtc.taskTypes:
                parser = utils.ProcessLogParser.fromTaskTypes(rtc.taskTypes)
                if not parser.progress:
                    parser = None

            opts = {
                'stdout': subprocess.PIPE, 
                'stderr': subprocess.STDOUT,
                'uid': uid,
                'cpus': cpus,
                'env': env,
            }

            cmd, opts = Profiler.getSubprocessOpts(rtc.command, **opts)

            logger.info("Running command: %s", rtc.command)
            self.__logfp.write("[%s] Running process" % time.strftime("%Y-%m-%d %H:%M:%S"))
            self.__logfp.flush()
            p = subprocess.Popen(cmd, **opts)

            self.__pptr = p
            self.__pid = p.pid

            self.__hasStarted.set()
            logger.info("PID: %d", p.pid)

            self.updateMetrics()

            writeLog = self.__logfp.write
            r_pipe = self.__pptr.stdout 

            for line in iter(r_pipe.readline, ""):

                writeLog(line)

                self.__lastLog = line

                if parser:
                    prog = parser.parseProgress(line)
                    if prog is not None:
                        self.__progress = prog 

                if self.__isShutdown.is_set():
                    break

            self.__logfp.write("[%s] Process finished" % time.strftime("%Y-%m-%d %H:%M:%S"))
            self.__logfp.flush()
            
            try:
                retcode = p.wait()
            except OSError, e:
                if e.errno != errno.ECHILD:
                    if not self.__isShutdown.is_set():
                        raise

            r_pipe.close()

            logger.debug("Return code: %s", retcode)

        except Exception, e:
            if self.__isShutdown.is_set():
                logger.debug("Thread detected shutdown request. Leaving gracefully.")
            else:
                logger.warn("Failed to execute command: %s", e)
                logger.debug(traceback.format_exc())

        finally:
            self.__completed(retcode)

    def updateMetrics(self):
        """
        updateMetrics()

        Resample information about the currently running 
        process tree, and update member attributes. 

        i.e. rss 
        """
        # logger.debug("updateMetrics(): %r", self)

        rss_bytes = 0
        cpu_perc = 0

        do_disk_io = self._DO_DISK_IO
        if do_disk_io:
            disk_io = [0,0,0,0]

        try:
            root_pid = self.__pid
            p = psutil.Process(root_pid)

            for proc in chain([p], p.children(True)):

                this_pid = proc.pid

                if proc.status == psutil.STATUS_ZOMBIE:
                    continue

                try:
                    rss_bytes += proc.memory_info().rss
                except psutil.Error, e:
                    logger.debug("Error while getting memory data for pid %r: %s", this_pid, e)

                try:
                    cpu_perc += proc.cpu_percent(None)
                except psutil.Error, e:
                    logger.debug("Error while getting cpu data for pid %r: %s", this_pid, e)

                if do_disk_io:
                    try:
                        counters = proc.io_counters()
                    except psutil.Error, e:
                        logger.debug("Error while getting disk io data for pid %r: %s", this_pid, e)
                    else:
                        for i, val in enumerate(counters):
                            disk_io[i] += val

        except psutil.NoSuchProcess, e:
            return

        cpu_perc_int = int(round(cpu_perc))
        rssMb = rss_bytes / 1024 / 1024

        metrics = self.__metrics

        maxRss = max(rssMb, metrics['maxRssMb'])
        disk_io_t = ttypes.DiskIO(*disk_io) if do_disk_io else None

        metrics.update({
            'rssMb': rssMb,
            'maxRssMb': maxRss,
            'cpuPercent': cpu_perc_int,
            'diskIO': disk_io_t,
        })
        logger.debug("metrics: %r", metrics)

    def killProcess(self, block=True, reason=''):
        """
        killProcess(bool block=True, reason='') -> (list killed_pids, list not_killed)

        Stop the entire process tree

        Returns a tuple of two lists. The first list contains 
        the pids from the process tree that were successfully 
        stopped. The second list contains pids that were not 
        able to be stopped successfully.

        By default the call blocks until the attempt to kill 
        has completed. Set block=False to issue the kill async. 

        If the reason for killing the process is passes as a string,
        it will be added to the log footer.
        """
        self.__killReason = reason 

        if block:
            return self.__killProcess()

        # guards against repeat calls to kill while one async
        # call is already running
        if self.__killThread and self.__killThread.isAlive():
            return

        t = threading.Thread(target=self.__killProcess)
        t.start()
        self.__killThread = t
        return


    def __killProcess(self):
        pid = self.__pid
        if pid == -1:
            return 

        try:
            p = psutil.Process(pid)
        except psutil.NoSuchProcess:
            return

        children = p.get_children(recursive=True)

        self.__wasKilled.set()

        # kill the top parent
        self.__killOneProcess(p)

        # make sure each process in the tree is really dead
        killed = []
        not_killed = []

        for child in children:
            success = self.__killOneProcess(child)
            if success:
                killed.append(child.pid)
            else:
                not_killed.append(child.pid)

        return killed, not_killed

    def __killOneProcess(self, p):
        """
        __killOneProcess(psutil.Process p) -> bool

        Try and nicely stop a Process first, then kill it. 
        Return True if process was killed.
        """
        try:
            try: 
                p.wait(0.001)
            except psutil.TimeoutExpired: 
                pass

            if not p.is_running():
                return True

            pid = p.pid 

            logger.info("Asking nicely for pid %d (%s) to stop", pid, p.name)
            p.terminate()
            try: 
                p.wait(5)
            except psutil.TimeoutExpired: 
                pass

            if not p.is_running():
                return True

            logger.info("Killing pid %d (%s)", pid, p.name)
            p.kill()
            try: 
                p.wait(1)
            except psutil.TimeoutExpired: 
                pass

            if p.is_running():
                logger.warn("Failed to properly kill pid %d (taskId: %s)", pid, self.__rtc.taskId)
                return False 

        except psutil.NoSuchProcess:
            pass

        return True

    def __completed(self, retcode):
        logger.debug("Process completed: %r, (IsShutdown: %r)", self, self.__isShutdown.is_set())
        result = ttypes.RunTaskResult()
        result.maxRssMb = self.__metrics['maxRssMb']

        result.procId = self.__rtc.procId
        result.taskId = self.__rtc.taskId
        result.jobId = self.__rtc.jobId

        if self.__isShutdown.is_set():
            result.exitStatus = 1
            result.exitSignal = 86
            logger.info("Task closing gracefully from shutdown request")

        elif self.__wasKilled.is_set():
            result.exitStatus = 1
            result.exitSignal = retcode if retcode < 0 else -9
        
        elif retcode < 0:
            result.exitStatus = 1
            result.exitSignal = retcode
        
        else:
            result.exitStatus = retcode
            result.exitSignal = 0

        logger.info("Process result %s", result)
        if not conf.NETWORK_DISABLED:
            while True:
                try:
                    service, transport = client.getPlowConnection()
                    service.taskComplete(result)
                    transport.close()
                    break
                except Exception, e:
                    logger.warn("Error talking to plow server, %s, sleeping for 30 seconds", e)
                    time.sleep(30)

        ProcessMgr.processFinished(result, self.__cpus)

        if self.__logfp is not None:

            attrs = {
                'DiskIO': self.__metrics['diskIO'],
                'Cpus': len(self.__cpus),
            }

            if self.__killReason:
                attrs['Reason Killed'] = self.__killReason

            self.__logfp.writeLogFooterAndClose(result, attrs)
            self.__logfp = None



#
# Singleton Instances
#
Profiler = _SystemProfiler()
ResourceMgr = _ResourceManager()
ProcessMgr = _ProcessManager()
