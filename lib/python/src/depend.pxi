
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
cdef DependSpec initDependSpec(DependSpecT& t):
    cdef DependSpec spec = DependSpec()
    spec.setDependSpec(t)
    return spec

cdef class DependSpec:
    """
    DependSpec 


    Specify the dependency between two types
    
    :var type: :data:`.DependType`
    :var dependentJob: str id or :class:`.Job`
    :var dependOnJob: str id or :class:`.Job`
    :var dependentLayer: str id or :class:`.Layer`
    :var dependOnLayer: str id or :class:`.Layer`
    :var dependentTask: str id or :class:`.Task`
    :var dependOnTask: str id or :class:`.Task`
    
    """
    cdef DependSpecT spec

    def __init__(self, int type = 0, **kwargs):
        self.spec.type = <DependType_type>type

        if 'dependentJob' in kwargs:
            j = kwargs['dependentJob']
            if isinstance(j, Job):
                j = j.id
            self.dependentJob = j

        if 'dependOnJob' in kwargs:
            j = kwargs['dependOnJob']
            if isinstance(j, Job):
                j = j.id
            self.dependOnJob = j

        if 'dependentLayer' in kwargs:
            l = kwargs['dependentLayer']
            if isinstance(l, Layer):
                l = l.id
            self.dependentLayer = l

        if 'dependOnLayer' in kwargs:
            l = kwargs['dependOnLayer']
            if isinstance(l, Layer):
                l = l.id
            self.dependOnLayer = l

        if 'dependentTask' in kwargs:
            t = kwargs['dependentTask']
            if isinstance(t, Task):
                t = t.id
            self.dependentTask = t

        if 'dependOnTask' in kwargs:
            t = kwargs['dependOnTask']
            if isinstance(t, Task):
                t = t.id            
            self.dependOnTask = t

    cdef setDependSpec(self, DependSpecT& t):
        self.spec = t

    cdef DependSpecT toDependSpecT(self):
        return self.spec

    property type:
        def __get__(self): return self.spec.type
        def __set__(self, DependType_type val): 
            self.spec.type = val

    property dependentJob:
        def __get__(self): return self.spec.dependentJob
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependentJob = val
            self.spec.__isset.dependentJob = True

    property dependOnJob:
        def __get__(self): return self.spec.dependOnJob
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependOnJob = val
            self.spec.__isset.dependOnJob = True

    property dependentLayer:
        def __get__(self): return self.spec.dependentLayer
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependentLayer = val
            self.spec.__isset.dependentLayer = True

    property dependOnLayer:
        def __get__(self): return self.spec.dependOnLayer
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependOnLayer = val
            self.spec.__isset.dependOnLayer = True

    property dependentTask:
        def __get__(self): return self.spec.dependentTask
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependentTask = val
            self.spec.__isset.dependentTask = True

    property dependOnTask:
        def __get__(self): return self.spec.dependOnTask
        def __set__(self, val): 
            if not isinstance(val, str):
                raise TypeError("Expecting a job guid string")
            self.spec.dependOnTask = val
            self.spec.__isset.dependOnTask = True

    def create(self):
        """
        Create the dependency from the current settings
        Return the newly created Depend instance.

        Ensure that a valid :data:`.DependType` is set,
        as well as the corresponding dependent and dependsOn id.

        :returns: :class:`.Depend`
        """
        cdef Depend dep = create_depend(self)
        return dep


#######################
# Depend
#
cdef inline Depend initDepend(DependT& d):
    cdef Depend dep = Depend()
    dep.setDepend(d)
    return dep

cdef class Depend(PlowBase):
    """
    Depend 

    Represents an existing dependency between two types
    
    :var id: str 
    :var type: :data:`DependType`
    :var active: bool
    :var createdTime: long 
    :var satisfiedTime: long
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
        def __get__(self): return long(self._depend.createdTime)

    property satisfiedTime:
        def __get__(self): return long(self._depend.satisfiedTime)

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

    @reconnecting
    def drop(self):
        """Drop the dependency """
        conn().proxy().dropDepend(self.id)
        self._depend.active = False

    @reconnecting
    def activate(self):
        """Activate the dependency """
        conn().proxy().activateDepend(self.id)
        self._depend.active = True


def create_depend(DependSpec spec):
    """
    Create a new dependency from a DependSpec

    A DependSpec must have its type set, along with a 
    corresonding dependent id, and a dependOn id

    :param spec: :class:`.DependSpec` 
    :returns: :class:`.Depend`

    """
    cdef:
        DependT depT
        Depend dep

    conn().proxy().createDepend(depT, spec.toDependSpecT())
    dep = initDepend(depT)
    return dep

@reconnecting
def create_job_on_job_depend(Job job, Job onJob):
    """
    Make one job dependent on another
    This call is async and does not return anything.

    :param job: the :class:`.Job` which depends on another
    :param onJob: the :class:`.Job` which must finish first
    """   
    conn().proxy().createJobOnJobDepend(job.id, onJob.id)

@reconnecting
def create_layer_on_layer_depend(Layer layer, Layer onLayer):
    """
    Make one layer dependent on another
    This call is async and does not return anything.

    :param layer: the :class:`.Layer` which depends on another
    :param onLayer: the :class:`.Layer` which must finish first
    """   
    conn().proxy().createLayerOnLayerDepend(layer.id, onLayer.id)

@reconnecting
def create_layer_on_task_depend(Layer layer, Task onTask):
    """
    Make one layer dependent on another task
    This call is async and does not return anything.

    :param layer: the :class:`.Layer` which depends on a task
    :param onTask: the :class:`.Task` which must finish first
    """   
    conn().proxy().createLayerOnTaskDepend(layer.id, onTask.id)

@reconnecting
def create_task_by_task_depend(Layer layer, Layer onLayer):
    """
    Make each task of a layer dependent on the corresponding task
    of another layer, one by one.
    This call is async and does not return anything.

    :param layer: the :class:`.Layer` which depends on another
    :param onLayer: the :class:`.Layer` which has tasks that must finish first
    """   
    conn().proxy().createTaskByTaskDepend(layer.id, onLayer.id)

@reconnecting
def create_task_on_layer_depend(Task task, Layer onLayer):
    """
    Make one task dependent on another layer
    This call is async and does not return anything.

    :param task: the :class:`.Task` which depends on a layer
    :param onLayer: the :class:`.Layer` which must finish first
    """   
    conn().proxy().createTaskOnLayerDepend(task.id, onLayer.id)

@reconnecting
def create_task_on_task_depend(Task task, Task onTask):
    """
    Make one task dependent on another
    This call is async and does not return anything.

    :param task: the :class:`.Task` which depends on another
    :param onTask: the :class:`.Task` which must finish first
    """   
    conn().proxy().createTaskOnTaskDepend(task.id, onTask.id)

@reconnecting
def get_depends_on_job(Job job):
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

    conn().proxy().getDependsOnJob(deps, job.id)
    ret = [initDepend(d) for d in deps]
    return ret 

@reconnecting
def get_job_depends_on(Job job):
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

    conn().proxy().getJobDependsOn(deps, job.id)
    ret = [initDepend(d) for d in deps]
    return ret  

@reconnecting
def get_depends_on_layer(Layer layer):
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

    conn().proxy().getDependsOnLayer(deps, layer.id)
    ret = [initDepend(d) for d in deps]
    return ret 

@reconnecting
def get_layer_depends_on(Layer layer):
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

    conn().proxy().getLayerDependsOn(deps, layer.id)
    ret = [initDepend(d) for d in deps]
    return ret   


@reconnecting
def get_depends_on_task(Task task):
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

    conn().proxy().getDependsOnTask(deps, task.id)
    ret = [initDepend(d) for d in deps]
    return ret    

@reconnecting
def get_task_depends_on(Task task):
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

    conn().proxy().getTaskDependsOn(deps, task.id)
    ret = [initDepend(d) for d in deps]
    return ret 

@reconnecting
def drop_depend(Depend dep):
    """
    Drop the depends 

    :param dep: :class:`.Depend` 
    """
    conn().proxy().dropDepend(dep.id)

@reconnecting
def activate_depend(Depend dep):
    """
    Reactivate the depends 

    :param dep: :class:`.Depend` 
    """    
    conn().proxy().activateDepend(dep.id)









