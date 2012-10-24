
import threading
import subprocess
import logging
import time
from datetime import datetime

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
        self.__slots = dict([(i, 0) for i in range(0, Profiler.physicalCpus)])
        self.__lock = threading.Lock()
        logger.info("Intializing resource manager with %d physical cores." % Profiler.physicalCpus)

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
            logger.info("Checked out CPUS: %s" % result)
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
                    logger.warn("Failed to check in core: %d" + core)
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
        self.sendPing(True)

    def runProcess(self, processCmd):
        cpus = ResourceMgr.checkout(processCmd.cores)
        pthread = ProcessThread(processCmd)
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
                logger.warn("Process %s not found: %s" % (processCmd.procId, e))

    def sendPing(self, isReboot=False):
        tasks = self.getRunningTasks()
        Profiler.sendPing(tasks, isReboot)

        self.__timer = threading.Timer(60.0, self.sendPing)
        self.__timer.daemon = True
        self.__timer.start()

    def killRunningTask(self, rTask):
        logger.info("kill requested for task %s" % rTask)

        with self.__lock:
            try:
                pthread = self.__threads[rTask.procId][1]
            except KeyError:
                err = "Process %s not found" % rTask.procId
                logger.warn(err)
                # TODO: Raise a proper exception type? or
                # fail quietly?
                raise ttypes.RndException(1, err)

        _, not_killed = pthread.killProcess()

        if not_killed:
            err = "Failed to kill the following pids for task %s: %s" % \
                    (rTask.taskId, ','.join(not_killed))
            logger.warn(err)
            raise ttypes.RndException(1, err)


    def getRunningTasks(self):
        return [t[1].getRunningTask() for t in self.__threads.itervalues()]



class ProcessThread(threading.Thread):
    """
    The ProcessThread wraps a running task.
    """
    
    def __init__(self, rtc):
        threading.Thread.__init__(self)
        self.daemon = True

        self.__rtc = rtc
        self.__pptr = None
        self.__logfp = None
        self.__pid = -1

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
        retcode = 1
        try:
            logger.info("Opening log file: %s" % self.__rtc.logFile)
            self.__logfp = open(self.__rtc.logFile, "w")
            self.__writeLogHeader()
            self.__logfp.flush()

            logger.info("Running command: %s" % self.__rtc.command)
            self.__pptr = subprocess.Popen(self.__rtc.command,
                shell=False, stdout=self.__logfp, stderr=self.__logfp)
            
            self.__pid = self.__pptr.pid
            logger.info("PID: %d" % self.__pid)
            retcode = self.__pptr.wait()
        
        except Exception, e:
            logger.warn("Failed to execute command: %s" % e)
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
        self.__writeLogFooter(result)
        self.__logfp.close()

    def __writeLogHeader(self):
        self.__logfp.write("Render Process Begin\n")
        self.__logfp.write("================================================================\n")

    def __writeLogFooter(self, result):
        # TODO: Add more stuff here
        self.__logfp.flush()
        self.__logfp.write("\n\n\n")
        self.__logfp.write("Render Process Complete\n")
        self.__logfp.write("=====================================\n")
        self.__logfp.write("Exit Status: %d\n" % result.exitStatus)
        self.__logfp.write("Signal: %d\n" % result.exitSignal)
        self.__logfp.write("MaxRSS: 0\n")
        self.__logfp.write("=====================================\n\n")

Profiler    = _SystemProfiler()
ResourceMgr = _ResourceManager()
ProcessMgr  = _ProcessManager()








