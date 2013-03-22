


#######################
# Tasks
#

cdef class Task:
    def __init__(self):
        raise NotImplementedError

# def get_task(Guid& taskId):
#     pass

# def get_tasks(TaskFilter filter):
#     getClient().proxy().getTasks()

def get_task_log_path(Guid& taskId):
    cdef: 
        string path
    getClient().proxy().getTaskLogPath(path, taskId)
    return path

# def retry_tasks(TaskFilter filter):
#     pass

# def eat_tasks(TaskFilter filter):
#     pass

# def kill_tasks(TaskFilter filter):
#     pass


cdef TaskTotals initTaskTotals(TaskTotalsT& t):
    cdef TaskTotals totals = TaskTotals()
    totals.setTaskTotals(t)
    return totals


cdef class TaskTotals:

    cdef TaskTotalsT _totals

    cdef setTaskTotals(self, TaskTotalsT& t):
        self._totals = t 

    property total:
        def __get__(self):
            return self._totals.totalTaskCount

    property succeeded:
        def __get__(self):
            return self._totals.succeededTaskCount

    property running:
        def __get__(self):
            return self._totals.runningTaskCount

    property dead:
        def __get__(self):
            return self._totals.deadTaskCount

    property eaten:
        def __get__(self):
            return self._totals.eatenTaskCount

    property waiting:
        def __get__(self):
            return self._totals.waitingTaskCount

    property depend:
        def __get__(self):
            return self._totals.dependTaskCount

                                
                                