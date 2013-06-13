from plow import client

#
# Display names
#
TIME_NO_TIME = "__-__ __:__:__"

TIME_NO_DURATION = "__:__:__"

# Map TaskState constants to a list of strings
TASK_STATES = sorted((a for a in dir(client.TaskState) if not a.startswith('_')), 
					 key=lambda x: getattr(client.TaskState, x))

# Map JobState constants to a list of strings
JOB_STATES = sorted((a for a in dir(client.JobState) if not a.startswith('_')), 
					 key=lambda x: getattr(client.JobState, x))

del client