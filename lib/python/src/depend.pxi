
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
    
    :var type: :data:`.DependType`
    :var dependentJob: str
    :var dependOnJob: str
    :var dependentLayer: str
    :var dependOnLayer: str
    :var dependentTask: str
    :var dependOnTask: str
    
    """
    cdef public DependType_type type
    cdef string dependentJob, dependOnJob, dependentLayer
    cdef string dependOnLayer, dependentTask, dependOnTask
    cdef _DependSpecT__isset __isset

    def __init__(self, **kwargs):
        self.type = kwargs.get('type', 0)

        if 'dependentJob' in kwargs:
            self.dependentJob = kwargs['dependentJob']
            self.__isset.dependentJob = True

        if 'dependOnJob' in kwargs:
            self.dependOnJob = kwargs['dependOnJob']
            self.__isset.dependOnJob = True

        if 'dependentLayer' in kwargs:
            self.dependentLayer = kwargs['dependentLayer']
            self.__isset.dependentLayer = True

        if 'dependOnLayer' in kwargs:
            self.dependOnLayer = kwargs['dependOnLayer']
            self.__isset.dependOnLayer = True

        if 'dependentTask' in kwargs:
            self.dependentTask = kwargs['dependentTask']
            self.__isset.dependentTask = True

        if 'dependOnTask' in kwargs:
            self.dependOnTask = kwargs['dependOnTask']
            self.__isset.dependOnTask = True

    cdef DependSpecT toDependSpecT(self):
        cdef DependSpecT s

        s.type = self.type
        s.dependentJob = self.dependentJob
        s.dependOnJob = self.dependOnJob
        s.dependentLayer = self.dependentLayer
        s.dependOnLayer = self.dependOnLayer
        s.dependentTask = self.dependentTask
        s.dependOnTask = self.dependOnTask
        s.__isset = self.__isset

        return s

    property dependentJob:
        def __get__(self): return self.dependentJob
        def __set__(self, val): 
            self.dependentJob = val
            self.__isset.dependentJob = True

    property dependOnJob:
        def __get__(self): return self.dependOnJob
        def __set__(self, val): 
            self.dependOnJob = val
            self.__isset.dependOnJob = True

    property dependentLayer:
        def __get__(self): return self.dependentLayer
        def __set__(self, val): 
            self.dependentLayer = val
            self.__isset.dependentLayer = True

    property dependOnLayer:
        def __get__(self): return self.dependOnLayer
        def __set__(self, val): 
            self.dependOnLayer = val
            self.__isset.dependOnLayer = True

    property dependentTask:
        def __get__(self): return self.dependentTask
        def __set__(self, val): 
            self.dependentTask = val
            self.__isset.dependentTask = True

    property dependOnTask:
        def __get__(self): return self.dependOnTask
        def __set__(self, val): 
            self.dependOnTask = val
            self.__isset.dependOnTask = True


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
    :var active: bool
    :var createdTime: long 
    :var modifiedTime: long
    :var dependentJobId: Guid
    :var dependOnJobId: Guid
    :var dependentLayerId: Guid
    :var dependOnLayerId: Guid
    :var dependentTaskId: Guid
    :var dependOnTaskId: Guid
    :var dependentJobName: str
    :var dependOnJobName: str
    :var dependentLayerName: str
    :var dependOnLayerName: str
    :var dependentTaskName: str
    :var dependOnTaskName: str

    """
    cdef DependT _depend

    cdef setDepend(self, DependT& d):
        self._depend = d

    property id:
        def __get__(self): return self._depend.id

    property type:
        def __get__(self): return self._depend.type
    
    property active:
        def __get__(self): return self._depend.active

    property createdTime:
        def __get__(self): return self._depend.createdTime

    property modifiedTime:
        def __get__(self): return self._depend.modifiedTime

    property dependentJobId:
        def __get__(self): return self._depend.dependentJobId

    property dependOnJobId:
        def __get__(self): return self._depend.dependOnJobId

    property dependentLayerId:
        def __get__(self): return self._depend.dependentLayerId

    property dependOnLayerId:
        def __get__(self): return self._depend.dependOnLayerId

    property dependentTaskId:
        def __get__(self): return self._depend.dependentTaskId

    property dependOnTaskId:
        def __get__(self): return self._depend.dependOnTaskId

    property dependentJobName:
        def __get__(self): return self._depend.dependentJobName

    property dependOnJobName:
        def __get__(self): return self._depend.dependOnJobName

    property dependentLayerName:
        def __get__(self): return self._depend.dependentLayerName

    property dependOnLayerName:
        def __get__(self): return self._depend.dependOnLayerName

    property dependentTaskName:
        def __get__(self): return self._depend.dependentTaskName

    property dependOnTaskName:
        def __get__(self): return self._depend.dependOnTaskName

    def drop(self):
        """
        Drop the dependency

        :returns: bool success
        """
        cdef bint ret 
        ret = drop_depend(self)
        if ret:
            self.active = False
        return ret

    def reactivate(self):
        """
        Reactivate the dependency 

        :returns: bool success
        """
        cdef bint ret 
        ret = reactivate_depend(self)
        if ret:
            self.active = True
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









