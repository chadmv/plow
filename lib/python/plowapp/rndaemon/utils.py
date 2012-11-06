
import re
from ast import literal_eval

import conf


class LogParser(object):
	"""
	LogParser 

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
			except ValueError, SyntaxError:
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

