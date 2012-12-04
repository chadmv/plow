
import os 
import time
import re
import errno
import logging
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
    def __init__(self, name, mode='w', buffering=-1):
        self.makeLogDir(name)
        self._fileObj = open(name, mode, buffering)

    def __del__(self):
        if self._fileObj is not None:
            self._fileObj.close()

    def __getattr__(self, name):
        return getattr(self._fileObj, name)

    def writeLogHeader(self, rtc):
        self._fileObj.write(
            "Render Process Begin\n" \
            "================================================================\n"
        )
        self._fileObj.write("COMMAND: %s\n" % " ".join(rtc.command))
        for key, value in rtc.env.items():
            self._fileObj.write("ENV: %s=%s\n" % (key, value))
        self._fileObj.write("================================================================\n")

        self._fileObj.flush()

    def writeLogFooterAndClose(self, result):
        # TODO: Add more stuff here
        # Check to ensure the log is not None, which it would be
        # if the thread failed to open the log file.
        if self._fileObj.closed:
            return

        self._fileObj.flush()
        self._fileObj.write(
            "\n\n\n" \
            "Render Process Complete\n" \
            "=====================================\n" \
            "Exit Status: %d\n" \
            "Signal: %d\n" \
            "MaxRSS: 0\n" \
            "=====================================\n\n" \
            % (result.exitStatus, result.exitSignal))

        self._fileObj.close() 

    @staticmethod 
    def makeLogDir(path):
        """
        makeLogDir(str path) -> void

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
            numTries += 1


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
