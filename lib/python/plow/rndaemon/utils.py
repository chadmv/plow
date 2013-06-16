
import os 
import time
import re
import errno
import logging
import socket
from ast import literal_eval

import conf

logger = logging.getLogger(__name__)


class ProcessLog(object):
    """
    ProcessLog 

    Wraps a file object to provide methods related 
    to common logging format of a running task. 

    Creates directories, and opens the given filename. 
    Writes headers and footers. 

    Passes all standard file methods through to the file 
    object. 
    """
    def __init__(self, name, mode='w', buffering=-1, uid=None):
        old_mask = os.umask(0)
        self.makeLogDir(name)
        self._fileObj = open(name, mode, buffering)
        os.umask(old_mask)

        self.__host = socket.getfqdn()

        if uid is None:
            return

        folder = os.path.dirname(name)
        gid = os.getgid()

        for elem in (folder, name):
            try:
                os.chmod(elem, 0777)
            except Exception, e:
                logger.warn("Failed to chmod path %r to 0777: %s", elem, e)

            try:
                os.chown(elem, uid, gid)
            except Exception, e:
                logger.warn("Failed to chown path %r to uid %r / gid %r: %s", elem, uid, gid, e)


    def __del__(self):
        try:
            self._fileObj.close()
        except:
            pass

    def __getattr__(self, name):
        return getattr(self._fileObj, name)

    def writeLogHeader(self, rtc):
        fileObj = self._fileObj

        now = time.strftime("%Y-%m-%d %H:%M:%S")
        fileObj.write("[%s] Render Process Begin\n" \
                      "===============================================\n" % now )

        fileObj.write("HOST: %s\n" % self.__host)
        fileObj.write("COMMAND: %s\n" % " ".join(rtc.command))
        
        for key, value in rtc.env.items():
            fileObj.write("ENV: %s=%s\n" % (key, value))
        
        fileObj.write("===============================================\n\n")

        fileObj.flush()

    def writeLogFooterAndClose(self, result, attrs=None):
        """
        writeLogFooterAndClose(RunTaskResult, result, dict attrs=None)
        
        Write out the log footer, from a RunTaskResult object,
        adding any optional key:value data passed in an attr dictionary. 

        Closes the log file when done.
        """
        fileObj = self._fileObj
        if not fileObj or fileObj.closed:
            return

        fileObj.flush()

        if attrs:
            extra = '\n'.join('%s: %s' % (k,v) for k,v in attrs.iteritems()) + "\n"
        else:
            extra = ''

        now = time.strftime("%Y-%m-%d %H:%M:%S")
        fileObj.write(
            "\n\n\n" \
            "[{now}] Render Process Complete\n" \
            "===============================================\n" \
            "Host: {host}\n" \
            "Exit Status: {status}\n" \
            "Signal: {signal}\n" \
            "MaxRSS: {maxRss}\n" \
            "{extra}" \
            "===============================================\n\n"\
            .format(
                now = now, 
                host = self.__host, 
                status = result.exitStatus, 
                signal = result.exitSignal, 
                maxRss = result.maxRssMb,
                extra = extra,
            ))

        fileObj.close() 

    @staticmethod 
    def makeLogDir(path):
        """
        makeLogDir(str path) -> void

        Make sure the directory for the task logs exist.  There is
        the potential for a race condition here due to NFS caching.
        """
        folder = os.path.dirname(path)
        if os.path.exists(folder):
            logger.debug("Log directory already exists: %r", folder)
            return

        numTries = 0
        maxTries = 8
        sleep = 10

        while True:
            if numTries >= maxTries:
                raise Exception("Failed creating log path after %d tries." % numTries)
            try:
                os.makedirs(folder, mode=0777)
            except OSError, exp:
                logger.warn("Error creating log path: %s, %s %d", folder, exp, exp.errno)
                if exp.errno != errno.EEXIST:
                    # If it already exists, clear the NFS cache for the parent
                    # which should make the directory visible to os.path.exists
                    os.utime(os.path.dirname(folder), None)

            if os.path.exists(folder):
                break

            time.sleep(sleep)
            numTries += 1

        logger.debug("Created log directory: %r", folder)


class ProcessLogParser(object):
    """
    ProcessLogParser 

    Provides pattern matching operations on lines from log 
    files, matching a given set of regular expression. 
    """

    def __init__(self, progPatterns=None):
        if progPatterns:
            self.progress = re.compile('|'.join('(?:%s)' % r for r in progPatterns if r))
        else:
            self.progress = None

    def parseProgress(self, line):
        """
        parseProgress(str line) -> float

        Take a string line and attempt to parse a progress value. 
        On success, returns a float 0.0 - 1.0
        Otherwise return None 
        """
        if not self.progress:
            return None 

        prog = self._parseLine(self.progress, line)
        if not prog:
            return None

        prog_val = 0.0

        if prog[-1] == '%':
            try:
                prog_val = literal_eval(prog[:-1])
            except (ValueError, SyntaxError):
                pass
            else:
                return prog_val / 100.0

        try:
            prog_val = literal_eval(prog)

        except ValueError:

            try:
                a, b = prog.split('/', 1)
                prog_val = float(a) / float(b)

            except ValueError:
                return None

            except ZeroDivisionError:
                prog_val = 0.0

        if 1 < prog_val <= 100:
            prog_val /= 100.0

        return prog_val

    @classmethod 
    def fromTaskTypes(cls, taskTypes):
        """
        fromTaskTypes(str|list taskTypes) -> LogParser

        Return a LogParser instance that is set up to parse 
        the given task types. 

        `taskTypes` may be either a single string, or a list 
        of string task types. They are looked up in the rndaemon 
        config for matching defined regular expression patterns. 
        """
        if isinstance(taskTypes, (str, unicode)):
            taskTypes = [taskTypes]

        progPatterns = filter(None, (conf.TASK_PROGRESS_PATTERNS.get(t) for t in taskTypes))
        parser = cls(progPatterns=progPatterns)

        return parser

    @staticmethod 
    def _parseLine(pattern, line):
        """
        Find the first capture group of the line
        """
        match = re.search(pattern, line.rstrip())
        if match:
            return next((i for i in match.groups() if i), None)

        return None
