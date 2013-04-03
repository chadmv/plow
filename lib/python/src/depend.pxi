
#######################
# DependType
#
@cython.internal
cdef class _DependType:
    cdef:
        readonly int JOB_ON_JOB 
        readonly int LAYER_ON_LAYER 
        readonly int LAYER_ON_TASK 
        readonly int TASK_ON_LAYER
        readonly int TASK_ON_TASK
        readonly int TASK_BY_TASK

    def __cinit__(self):
        self.JOB_ON_JOB = JOB_ON_JOB
        self.LAYER_ON_LAYER = LAYER_ON_LAYER
        self.LAYER_ON_TASK = LAYER_ON_TASK
        self.TASK_ON_LAYER = TASK_ON_LAYER
        self.TASK_ON_TASK = TASK_ON_TASK
        self.TASK_BY_TASK = TASK_BY_TASK

DependType = _DependType()


#######################
# DependSpec
#
cdef class DependSpec:
    """
    DependSpec 

    TODO
    """
    cdef public string dependentJob, dependOnJob, dependentLayer
    cdef public string dependOnLayer, dependentTask, dependOnTask

    def __init__(self, **kwargs):
        self.dependentJob = kwargs.get('dependentJob', '')
        self.dependOnJob = kwargs.get('dependOnJob', '')
        self.dependentLayer = kwargs.get('dependentLayer', '')
        self.dependOnLayer = kwargs.get('dependOnLayer', '')
        self.dependentTask = kwargs.get('dependentTask', '')
        self.dependOnTask = kwargs.get('dependOnTask', '')

    cdef DependSpecT toDependSpecT(self):
        cdef DependSpecT s

        s.dependentJob = self.dependentJob
        s.dependOnJob = self.dependOnJob
        s.dependentLayer = self.dependentLayer
        s.dependOnLayer = self.dependOnLayer
        s.dependentTask = self.dependentTask
        s.dependOnTask = self.dependOnTask

        return s