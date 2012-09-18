
import threading
import subprocess
import logging
import time

import netcode
import conf
import rpc.ttypes as ttypes

from datetime import datetime

from profile.macosx import SystemProfiler

logger = logging.getLogger("librnd.core")

class ResourceManager(object):

    def __init__(self):
        self.__slots = dict([(i, 0) for i in range(0, Profiler.physicalCpus)])
        self.__lock = threading.Lock()

    def checkout(self, numCores):
        self.__lock.acquire(True)
        try:
            open_slots = self.getOpenSlots()
            if numCores > len(open_slots):
                raise Exception("No more open slots")
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
                self.__slots[core] = 0
        finally:
            self.__lock.release()
        logger.info("Checked in CPUS: %s" % cores)

    def getSlots(self):
        return dict(self.__slots)

    def getOpenSlots(self):
        return [slot for slot in self.__slots if self.__slots[slot] == 0]

class ProcessManager(object):

    def __init__(self):
        self.__threads = { }
        self.__lock = threading.Lock()
        self.sendPing(True)

    def runProcess(self, processCmd):
        cpus = ResourceMgr.checkout(processCmd.cores)
        pthread = ProcessThread(processCmd)
        with self.__lock:
            self.__threads[processCmd.procId] = (processCmd, pthread, cpus)
        pthread.run()
        return pthread.getProcess()

    def processFinished(self, processCmd):
        ResourceMgr.checkin(self.__threads[processCmd.procId][2])
        with self.__lock:
            try:
                del self.__threads[processCmd.procId]
            except Exception, e:
                logger.warn("Process %s not found: %s" % (processCmd.procId, e))

    def sendPing(self, isReboot=False):
        processes = [p[1].getProcess() for p in self.__threads]
        Profiler.sendPing(processes, isReboot)

        self.__timer = threading.Timer(60.0, self.sendPing)
        self.__timer.daemon = True
        self.__timer.start()


class ProcessThread(threading.Thread):

    def __init__(self, processCmd):
        threading.Thread.__init__(self)
        self.daemon = True

        self.processCmd = processCmd
        self.__pptr = None
        self.__logfp = None

        self.__process = ttypes.Process()
        self.__process.procId = processCmd.procId
        self.__process.frameId = processCmd.frameId
        self.__process.maxRss = 0
        self.__process.pid = 0

    def getProcess(self):
        return self.__process

    def run(self):
        retcode = 1
        try:
            
            logger.info("Opening log file: %s" % self.processCmd.logFile)
            self.__logfp = open(self.processCmd.logFile, "w")
            self.__writeLogHeader()
            self.__logfp.flush()

            logger.info("Running command: %s" % self.processCmd.command)
            self.__pptr = subprocess.Popen(self.processCmd.command, 
                shell=False, stdout=self.__logfp, stderr=self.__logfp)
            
            self.__process.pid = self.__pptr.pid
            logger.info("PID: %d" % self.__process.pid)
            retcode = self.__pptr.wait()
        
        except Exception, e:
            logger.warn("Failed to execute command: %s" % e)
        finally:
            self.__completed(retcode)

    def __completed(self, retcode):

        result = ttypes.ProcessResult()
        result.process = self.__process
        if retcode < 0:
            result.exitStatus = 1
            result.signal = retcode
        else:
            result.exitStatus = retcode
            result.signal = 0

        logger.info("Process result %s" % result)
        if not conf.NETWORK_DISABLED:
            while True:
                try:
                    conn = netcode.getPlowConnection()
                    conn.processCompleted(result)
                    break
                except Exception, e:
                    logger.info("Plow server is down, sleeping for 30 seconds")
                    time.sleep(30)

        ProcessMgr.processFinished(self.processCmd)
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
        self.__logfp.write("Signal: %d\n" % result.signal)
        self.__logfp.write("MaxRSS: %d\n" % self.__process.maxRss)
        self.__logfp.write("=====================================\n\n")


Profiler = SystemProfiler()
ResourceMgr = ResourceManager()
ProcessMgr = ProcessManager()

def runProcess(command):
    return ProcessMgr.runProcess(command)







