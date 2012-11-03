
import threading
import subprocess
import logging
import time
import os
import errno
import traceback

import psutil

import conf
import client
import rpc.ttypes as ttypes

from profile import SystemProfiler as _SystemProfiler

logger = logging.getLogger(__name__)

__all__ = ['Profiler', 'ResourceMgr', 'ProcessMgr']

class _ResourceManager(object):
    """
    The ResourceManager keeps track of the bookable resources on the
    machine.  This is currently just cores, but memory and GPUS
    in the future.
    """

    def __init__(self):
        self.__slots = dict((i, 0) for i in xrange(Profiler.physicalCpus))
        self.__lock = threading.Lock()
        logger.info("Intializing resource manager with %d physical cores.", Profiler.physicalCpus)

    def checkout(self, numCores):
        if numCores < 1:
            raise ttypes.RndException(1, "Cannot reserve 0 slots")

        self.__lock.acquire(True)
        try:
            open_slots = self.getOpenSlots()
            logger.info(open_slots)
            if numCores > len(open_slots):
                raise ttypes.RndException(1, "No more open slots")
            result = open_slots[0:numCores]
            for i in result:
                self.__slots[i] = 1
            logger.info("Checked out CPUS: %s", result)
            return result
        finally:
            self.__lock.release()

    def checkin(self, cores):
        self.__lock.acquire(True)
        try:
            for core in cores:
                if self.__slots[core] == 1:
                    self.__slots[core] = 0
                else:
                    logger.warn("Failed to check in core: %d", core)
        finally:
            self.__lock.release()
        logger.info("Checked in CPUS: %s" % cores)

    def getSlots(self):
        return dict(self.__slots)

    def getOpenSlots(self):
        return [slot for slot in self.__slots if self.__slots[slot] == 0]

class _ProcessManager(object):
    """
    The ProcessManager keeps track of the running tasks.  Each task
    is executed in a separate ProcessThread.
    """

    def __init__(self):
        self.__threads = { }
        self.__lock = threading.Lock()
        self.__timer = None 
        self.__isReboot = threading.Event()

        self.sendPing(True)

    def runProcess(self, processCmd):
        cpus = ResourceMgr.checkout(processCmd.cores)
        pthread = ProcessThread(processCmd, cpus)
        with self.__lock:
            self.__threads[processCmd.procId] = (processCmd, pthread, cpus)
        pthread.start()
        logger.info("procsss thread started")
        return pthread.getRunningTask()

    def processFinished(self, processCmd):
        ResourceMgr.checkin(self.__threads[processCmd.procId][2])
        with self.__lock:
            try:
                del self.__threads[processCmd.procId]
            except Exception, e:
                logger.warn("Process %s not found: %s", processCmd.procId, e)

    def sendPing(self, isReboot=False, repeat=True):
        # TODO: What is the purpose of the isReboot flag?
        # Using the internal flag to determine if we are in a 
        # reboot state.
        isReboot = self.__isReboot.isSet()

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

    def killRunningTask(self, procId):
        logger.info("kill requested for procId %s" % procId)

        with self.__lock:
            try:
                pthread = self.__threads[procId][1]
            except KeyError:
                err = "Process %s not found" % procId
                logger.warn(err)
                # TODO: Raise a proper exception type? or
                # fail quietly?
                raise ttypes.RndException(1, err)

        _, not_killed = pthread.killProcess()

        if not_killed:
            err = "Failed to kill the following pids for prodId %s: %s" % \
                    (procId, ','.join(not_killed))
            logger.warn(err)
            raise ttypes.RndException(1, err)


    def getRunningTasks(self):
        return [t[1].getRunningTask() for t in self.__threads.itervalues()]


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


class ProcessThread(threading.Thread):
    """
    The ProcessThread wraps a running task.
    """
    
    def __init__(self, rtc, cpus=None):
        threading.Thread.__init__(self)
        self.daemon = True

        self.__rtc = rtc
        self.__pptr = None
        self.__logfp = None
        self.__pid = -1

        self.__cpus = cpus or set()

    def __repr__(self):
        return "<%s: (procId: %s, pid: %d)>" % (
            self.__class__.__name__, 
            self.__rtc.procId, 
            self.__pid)

    def getRunningTask(self):
        rt = ttypes.RunningTask()
        rt.jobId = self.__rtc.jobId
        rt.procId = self.__rtc.procId
        rt.taskId = self.__rtc.taskId
        rt.maxRss = 0
        rt.pid = self.__pid
        return rt

    def run(self):
        rtc = self.__rtc 
        retcode = 1
        try:
            self.__makeLogDir(rtc.logFile)
            logger.info("Opening log file: %s" % rtc.logFile)
            self.__logfp = open(rtc.logFile, "w")
            self.__writeLogHeader()
            self.__logfp.flush()

            opts = {
                'stdout': self.__logfp, 
                'stderr': subprocess.STDOUT,
                'uid'   : self.__rtc.uid,
                'cpus'  : self.__cpus,
            }

            cmd, opts = Profiler.getSubprocessOpts(rtc.command, **opts)

            logger.info("Running command: %s" % rtc.command)
            self.__pptr = subprocess.Popen(cmd, **opts)
            
            self.__pid = self.__pptr.pid
            logger.info("PID: %d" % self.__pid)
            retcode = self.__pptr.wait()
        
        except Exception, e:
            logger.warn("Failed to execute command: %s" % e)
            logger.debug(traceback.format_exc())

        finally:
            self.__completed(retcode)


    def killProcess(self):
        """
        killProcess() -> (list killed_pids, list not_killed)

        Stop the entire process tree

        Returns a tuple of two lists. The first list contains 
        the pids from the process tree that were successfully 
        stopped. The second list contains pids that were not 
        able to be stopped successfully.
        """
        p = psutil.Process(self.__pid)
        children = p.get_children(recursive=True)

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

    def __makeLogDir(self, path):
        """
        __makeLogDir(path) -> void

        Make sure the directory for the task logs exist.  There is
        the potential for a race condition here due to NFS caching.
        """
        folder = os.path.dirname(path)
        if os.path.exists(folder):
            return

        numTries = 0
        maxTries = 8
        sleep = 10
        
        while True:
            if numTries >= maxTries:
                raise Exception("Failed creating log path after %d tries." % numTries)
            try:
                os.makedirs(folder, 0777)
            except OSError, exp:
                logger.warn("Error creating log path: %s, %s %d", folder, exp, exp.errno)
                if exp.errno != errno.EEXIST:
                    # If it already exists, clear the NFS cache for the parent
                    # which should make the directory visible to os.path.exists
                    os.utime(os.path.dirname(folder), None)
            
            if os.path.exists(folder):
                return
            
            time.sleep(sleep)
            numTries+=1

    def __killOneProcess(self, p):
        """
        __killOneProcess(psutil.Process p) -> bool

        Try and nicely stop a Process first, then kill it. 
        Return True if process was killed.
        """
        try: p.wait(0.001)
        except psutil.TimeoutExpired: pass
        if not p.is_running():
            return True

        pid = p.pid 

        logger.info("Asking nicely for pid %d (%s) to stop" % (pid, p.name))
        p.terminate()
        try: p.wait(5)
        except psutil.TimeoutExpired: pass

        if not p.is_running():
            return True

        logger.info("Killing pid %d (%s)" % (pid, p.name))
        p.kill()
        try: p.wait(1)
        except psutil.TimeoutExpired: pass
        
        if p.is_running():
            logger.warn("Failed to properly kill pid %d (taskId: %s)" % \
                (pid, self.__rtc.taskId))    
            return False 

        return True


    def __completed(self, retcode):

        result = ttypes.RunTaskResult()
        result.procId = self.__rtc.procId
        result.taskId = self.__rtc.taskId
        result.jobId = self.__rtc.jobId
        result.maxRss = 0
        if retcode < 0:
            result.exitStatus = 1
            result.exitSignal = retcode
        else:
            result.exitStatus = retcode
            result.exitSignal = 0

        logger.info("Process result %s" % result)
        if not conf.NETWORK_DISABLED:
            while True:
                try:
                    service, transport = client.getPlowConnection()
                    service.taskComplete(result)
                    transport.close()
                    break
                except Exception, e:
                    logger.warn("Error talking to plow server," + str(e) + ", sleeping for 30 seconds")
                    time.sleep(30)

        ProcessMgr.processFinished(self.__rtc)
        self.__writeLogFooterAndClose(result)

    def __writeLogHeader(self):
        self.__logfp.write(
            "Render Process Begin\n" \
            "================================================================\n")

    def __writeLogFooterAndClose(self, result):
        # TODO: Add more stuff here
        # Check to ensure the log is not None, which it would be
        # if the thread failed to open the log file.
        if not self.__logfp:
            return
        self.__logfp.flush()
        self.__logfp.write(
            "\n\n\n" \
            "Render Process Complete\n" \
            "=====================================\n" \
            "Exit Status: %d\n" \
            "Signal: %d\n" \
            "MaxRSS: 0\n" \
            "=====================================\n\n" \
            % (result.exitStatus, result.exitSignal))
        self.__logfp.close()




Profiler    = _SystemProfiler()
ResourceMgr = _ResourceManager()
ProcessMgr  = _ProcessManager()







