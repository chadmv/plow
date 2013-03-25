


#######################
# Tasks
#

cdef class TaskSpec:

    cdef public string name
    cdef list depends

    def __init__(self, **kwargs):
        self.name = kwargs.get('name', '')
        self.depends = kwargs.get('depends', [])

    def __repr__(self):
        return "<TaskSpec: %s>" % self.name

    cdef TaskSpecT toTaskSpecT(self):
        cdef TaskSpecT s

        s.name = self.name 

        cdef: 
            DependSpecT specT
            DependSpec spec
        for spec in self.depends:
            specT = spec.toDependSpecT()
            s.depends.push_back(specT) 

        return s

    property depends:
        def __get__(self): return self.depends

        def __set__(self, val): self.depends = val


cdef inline Task initTask(TaskT& t):
    cdef Task task = Task()
    task.setTask(t)
    return task


cdef class Task:

    cdef TaskT _task 

    def __repr__(self):
        return "<Task: %s>" % self.name

    cdef setTask(self, TaskT& t):
        self._task = t

    property id:
        def __get__(self): return self._task.id

    property name:
        def __get__(self): return self._task.name

    property number:
        def __get__(self): return self._task.number

    property dependCount:
        def __get__(self): return self._task.dependCount

    property order:
        def __get__(self): return self._task.order

    property startTime:
        def __get__(self): return self._task.startTime

    property stopTime:
        def __get__(self): return self._task.stopTime

    property lastNodeName:
        def __get__(self): return self._task.lastNodeName

    property lastLogLine:
        def __get__(self): return self._task.lastLogLine

    property retries:
        def __get__(self): return self._task.retries

    property cores:
        def __get__(self): return self._task.cores

    property ramMb:
        def __get__(self): return self._task.ramMb

    property rssMb:
        def __get__(self): return self._task.rssMb

    property maxRssMb:
        def __get__(self): return self._task.maxRssMb

    property cpuPerc:
        def __get__(self): return self._task.cpuPerc

    property maxCpuPerc:
        def __get__(self): return self._task.maxCpuPerc

    property progress:
        def __get__(self): return self._task.progress

    property state:
        def __get__(self): 
            cdef int aState = self._task.progress
            return aState


def get_task(Guid& taskId):
    cdef:
        TaskT taskT 
        Task task 

    getClient().proxy().getTask(taskT, taskId)
    task = initTask(taskT)
    return task

# def get_tasks(TaskFilter filter):
#     getClient().proxy().getTasks()

def get_task_log_path(Guid& taskId):
    cdef string path
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

                                
                                