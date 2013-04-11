
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


    Specify the dependency between two types
    
    :var type: :data:`DependType`
    :var dependentJob: str
    :var dependOnJob: str
    :var dependentLayer: str
    :var dependOnLayer: str
    :var dependentTask: str
    :var dependOnTask: str
    
    """
    cdef public string dependentJob, dependOnJob, dependentLayer
    cdef public string dependOnLayer, dependentTask, dependOnTask
    cdef public DependType_type type

    def __init__(self, **kwargs):
        self.type = kwargs.get('type', 0)
        self.dependentJob = kwargs.get('dependentJob', '')
        self.dependOnJob = kwargs.get('dependOnJob', '')
        self.dependentLayer = kwargs.get('dependentLayer', '')
        self.dependOnLayer = kwargs.get('dependOnLayer', '')
        self.dependentTask = kwargs.get('dependentTask', '')
        self.dependOnTask = kwargs.get('dependOnTask', '')

    cdef DependSpecT toDependSpecT(self):
        cdef DependSpecT s

        s.type = self.type
        s.dependentJob = self.dependentJob
        s.dependOnJob = self.dependOnJob
        s.dependentLayer = self.dependentLayer
        s.dependOnLayer = self.dependOnLayer
        s.dependentTask = self.dependentTask
        s.dependOnTask = self.dependOnTask

        return s

#######################
# Depend
#
cdef inline Depend initDepend(DependT& d):
    cdef Depend dep = Depend()
    dep.setDepend(d)
    return dep

cdef class Depend:
    """
    Depend 

    Represents an existing dependency between two types
    
    :var id: str 
    :var type: :data:`DependType`
    :var dependentJob: str
    :var dependOnJob: str
    :var dependentLayer: str
    :var dependOnLayer: str
    :var dependentTask: str
    :var dependOnTask: str

    """
    cdef DependT _depend

    cdef setDepend(self, DependT& d):
        self._depend = d

    property id:
        def __get__(self): return self._depend.id

    property type:
        def __get__(self): return self._depend.type

    property dependentJob:
        def __get__(self): return self._depend.dependentJob

    property dependOnJob:
        def __get__(self): return self._depend.dependOnJob

    property dependentLayer:
        def __get__(self): return self._depend.dependentLayer

    property dependOnLayer:
        def __get__(self): return self._depend.dependOnLayer

    property dependentTask:
        def __get__(self): return self._depend.dependentTask

    property dependOnTask:
        def __get__(self): return self._depend.dependOnTask

    def drop(self):
        """
        Drop the dependency

        :returns: bool success
        """
        cdef bint ret 
        ret = drop_depend(self)
        return ret

    def reactivate(self):
        """
        Reactivate the dependency 

        :returns: bool success
        """
        cdef bint ret 
        ret = reactivate_depend(self)
        return ret        


cpdef inline get_depends_on_job(Job job):
    """
    Get a list of depends that others have 
    on this job

    :param job: :class:`.Job` 
    :returns: list[:class:`.Depend`]
    """
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getDependsOnJob(deps, job.id)
    ret = [initDepend(d) for d in deps]
    return ret 

cpdef inline get_job_depends_on(Job job):
    """
    Get a list of depends that this job has 
    on others

    :param job: :class:`.Job` 
    :returns: list[:class:`.Depend`]
    """    
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getJobDependsOn(deps, job.id)
    ret = [initDepend(d) for d in deps]
    return ret  

cpdef inline get_depends_on_layer(Layer layer):
    """
    Get a list of depends that others have 
    on this layer

    :param layer: :class:`.Layer` 
    :returns: list[:class:`.Depend`]
    """    
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getDependsOnLayer(deps, layer.id)
    ret = [initDepend(d) for d in deps]
    return ret 

cpdef inline get_layer_depends_on(Layer layer):
    """
    Get a list of depends that this layer has 
    on others

    :param layer: :class:`.Layer` 
    :returns: list[:class:`.Depend`]
    """        
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getLayerDependsOn(deps, layer.id)
    ret = [initDepend(d) for d in deps]
    return ret   

cpdef inline get_depends_on_task(Task task):
    """
    Get a list of depends that others have 
    on this task

    :param task: :class:`.Task` 
    :returns: list[:class:`.Depend`]
    """    
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getDependsOnTask(deps, task.id)
    ret = [initDepend(d) for d in deps]
    return ret    

cpdef inline get_task_depends_on(Task task):
    """
    Get a list of depends that this task has 
    on others

    :param task: :class:`.Task` 
    :returns: list[:class:`.Depend`]
    """        
    cdef:
        DependT d
        vector[DependT] deps 
        list ret

    getClient().proxy().getTaskDependsOn(deps, task.id)
    ret = [initDepend(d) for d in deps]
    return ret 

cpdef inline bint drop_depend(Depend dep) except? -1:
    """
    Drop the depends 

    :param dep: :class:`.Depend` 
    :return: bool success
    """
    cdef bint ret 
    ret = getClient().proxy().dropDepend(dep.id)
    return ret

cpdef inline bint reactivate_depend(Depend dep) except? -1:
    """
    Reactivate the depends 

    :param dep: :class:`.Depend` 
    :return: bool success
    """    
    cdef bint ret 
    ret = getClient().proxy().reactivateDepend(dep.id)
    return ret   









