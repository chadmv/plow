
#######################
# JobStats
#
cdef JobStats initJobStats(JobStatsT& t):
    cdef JobStats stats = JobStats()
    stats.setJobStats(t)
    return stats


cdef class JobStats:
    """
    Data structure representing stats for a Job

    :var highRam: int
    :var highCores: float
    :var highCoreTime: long
    :var totalCoreTime: long
    :var totalSuccessCoreTime: long
    :var totalFailCoreTime: long
    :var highClockTime: long
     
    """
    cdef JobStatsT _stats

    cdef setJobStats(self, JobStatsT& t):
        self._stats = t 

    property highRam:
        def __get__(self): return self._stats.highRam

    property highCores:
        def __get__(self): return self._stats.highCores

    property highCoreTime:
        def __get__(self): return self._stats.highCoreTime

    property highClockTime:
        def __get__(self): return self._stats.highClockTime

    property totalCoreTime:
        def __get__(self): return long(self._stats.totalCoreTime)

    property totalSuccessCoreTime:
        def __get__(self): return long(self._stats.totalSuccessCoreTime)

    property totalFailCoreTime:
        def __get__(self): return long(self._stats.totalFailCoreTime)


#######################
# JobState
#

@cython.internal
cdef class _JobState:
    cdef readonly int INITIALIZE, RUNNING, FINISHED

    def __cinit__(self):
        self.INITIALIZE = JOBSTATE_INITIALIZE
        self.RUNNING = JOBSTATE_RUNNING
        self.FINISHED = JOBSTATE_FINISHED

JobState = _JobState()



#######################
# JobFilter
#

@cython.internal
cdef class JobFilter:
    cdef JobFilterT value

    def __init__(self, **kwargs):
        self.value.matchingOnly = kwargs.get('matchingOnly', False)
        self.value.regex = kwargs.get('regex', '')
        self.value.jobIds = kwargs.get('jobIds', [])

        project = kwargs.get('project', [])
        if isinstance(project, (str, unicode)):
            project = [project]
        self.value.project = project
        
        user = kwargs.get('user', [])
        if isinstance(user, (str, unicode)):
            user = [user]
        self.value.user = user

        name = kwargs.get('name', [])
        if isinstance(name, (str, unicode)):
            name = [name]
        self.value.name = name

        cdef JobState_type i
        for i in kwargs.get('states', []):
            self.value.states.push_back(i)  


#######################
# JobSpec
#
cdef JobSpec initJobSpec(JobSpecT& t):
    cdef JobSpec spec = JobSpec()
    spec.setJobSpec(t)
    return spec

cdef class JobSpec:
    """
    A JobSpec specifies the parameters for 
    launching a job

    :var name: str
    :var project: str`.Project` code
    :var username: str - owner
    :var logPath: str - path to logfile
    :var paused: bool ; if True, submit in a paused state 
    :var uid: int - uid of owner 
    :var layers: list[:class:`.LayerSpec`]
    :var depends: list[:class:`.DependSpec`]
    :var attrs: dict
    :var env: dict

    """
    cdef public string name, project, username, logPath
    cdef public bint paused
    cdef public int uid
    cdef list layers, depends
    cdef dict attrs, env

    def __init__(self, **kwargs):
        self.name = kwargs.get('name', '')
        self.project = kwargs.get('project', '')
        self.username = kwargs.get('username', '')
        self.logPath = kwargs.get('logPath', '')
        self.paused = kwargs.get('paused', False)
        self.uid = kwargs.get('uid', 0)
        self.layers = kwargs.get('layers', []) 
        self.depends = kwargs.get('depends', [])
        self.attrs = kwargs.get('attrs', {})
        self.env = kwargs.get('env', {})

    def __repr__(self):
        return "<JobSpec: %s, %s>" % (self.name, self.project)

    cdef setJobSpec(self, JobSpecT& t):
        self.name = t.name
        self.project = t.project
        self.username = t.username
        self.logPath = t.logPath
        self.paused = t.paused
        self.uid = t.uid
        self.attrs = t.attrs
        self.env = t.env

        cdef DependSpecT dep
        self.depends = [initDependSpec(dep) for dep in t.depends]

        cdef LayerSpecT layer
        self.layers = [initLayerSpec(layer) for layer in t.layers]

    cdef JobSpecT toJobSpecT(self):
        cdef JobSpecT s

        s.name = self.name 
        s.project = self.project 
        s.username = self.username 
        s.logPath = self.logPath
        s.paused = self.paused 
        s.uid = self.uid
        s.attrs = self.attrs
        s.env = self.env

        cdef:
            LayerSpec aLayer
            LayerSpecT specT

        for aLayer in self.layers:
            specT = aLayer.toLayerSpecT()
            s.layers.push_back(specT)

        cdef:
            DependSpec aDep
            DependSpecT depT

        for aDep in self.depends:
            depT = aDep.toDependSpecT()
            s.depends.push_back(depT)

        return s

    property layers:
        def __get__(self): return self.layers
        def __set__(self, val): self.layers = val

    property depends:
        def __get__(self): return self.depends
        def __set__(self, val): self.depends = val

    property attrs:
        def __get__(self): return self.attrs
        def __set__(self, val): self.attrs = val

    property env:
        def __get__(self): return self.env
        def __set__(self, val): self.env = val

    def launch(self):
        """
        Launch this spec and return the Job 

        :returns: :class:`.Job`
        """
        cdef:
            JobT job
            JobSpecT spec
            Job ret 

        spec = self.toJobSpecT()
        conn().proxy().launch(job, spec)

        ret = initJob(job)
        return ret


@reconnecting
def get_job_spec(Guid& jobId):
    """
    Get a JobSpec instance from an existing
    job id Guid

    :param jobId: str
    :returns: :class:`.JobSpec`
    """
    cdef:
        JobSpecT specT
        JobSpec spec

    conn().proxy().getJobSpec(specT, jobId)
    spec = initJobSpec(specT)
    return spec


#######################
# Job
#
cdef inline Job initJob(JobT& j):
    cdef Job job = Job()
    job.setJob(j)
    return job


@cython.final
cdef class Job(PlowBase):
    """
    A Job 

    :var id: str 
    :var folderId: str 
    :var name: str - name of the job 
    :var username: str - name of job owner
    :var uid: int - uid of job owner
    :var state: :obj:`.JobState`
    :var paused: bool
    :var minCores: int 
    :var maxCores: int 
    :var runCores: int 
    :var startTime: long - msec since epoch
    :var stopTime: long - msec since epoch
    :var totals: :class:`.TaskTotals`
    :var stats: :class:`.JobStats`
    :var attrs: dict

    """
    cdef:
        JobT _job 
        TaskTotals _totals
        JobStats _stats

    def __init__(self):
        self._totals = None

    def __repr__(self):
        return "<Job: %s>" % self.name

    cdef setJob(self, JobT& j):
        self._job = j
        self._totals = initTaskTotals(self._job.totals)
        self._stats = initJobStats(self._job.stats)

    property id:
        def __get__(self): return self._job.id

    property folderId:
        def __get__(self): return self._job.folderId

    property name:
        def __get__(self): return self._job.name

    property username:
        def __get__(self): return self._job.username

    property uid:
        def __get__(self): return self._job.uid

    property state:
        def __get__(self): return self._job.state

    property paused:
        def __get__(self): return self._job.paused

    property minCores:
        def __get__(self): return self._job.minCores

    property maxCores:
        def __get__(self): return self._job.maxCores

    property runCores:
        def __get__(self): return self._job.runCores

    property startTime:
        def __get__(self): return long(self._job.startTime)

    property stopTime:
        def __get__(self): return long(self._job.stopTime)

    property totals:
        def __get__(self): return self._totals

    property stats:
        def __get__(self): return self._stats

    property attrs:
        def __get__(self): return self._job.attrs

    @reconnecting
    def refresh(self):
        """
        Refresh the attributes from the server
        """
        cdef JobT job 
        conn().proxy().getJob(job, self._job.id)
        self.setJob(job)

    def kill(self, string reason):
        """
        Kill the job 

        :param reason: str - reason for killing
        """
        kill_job(self, reason)

    def pause(self, bint paused):
        """
        Set the pause state of the job

        :param paused: bool
        """
        pause_job(self, paused)

    def get_outputs(self):
        """
        Get a list of outputs 

        :returns: list[:class:`.plowOutput`]
        """
        return get_job_outputs(self)

    def set_min_cores(self, int value):
        """
        Set the minimum cores the job should use 

        :param value: int 
        """
        set_job_min_cores(self, value)

    def set_max_cores(self, int value):
        """
        Set the maximum cores the job should use 

        :param value: int 
        """
        set_job_max_cores(self, value)

    def get_layers(self):
        """
        Get the layers for this job 

        :returns: list[:class:`.Layer`]
        """
        cdef list ret 
        ret = get_layers(self)
        return ret

    cpdef inline list get_tasks(self, list states=None):
        """
        Get a list of tasks for this job, optionally
        filtered by a list of task stats

        :param stats: list[:obj:`.TaskState`] = None
        :returns: list[:class:`.Task`]
        """
        cdef list ret 

        if states:
            ret = get_tasks(job=self, states=states)
        else:
            ret = get_tasks(job=self)

        return ret

    def get_depends(self):
        """
        Get a list of depends that others have 
        on this job

        :returns: list[:class:`.Depend`]
        """
        cdef list ret = get_depends_on_job(self)
        return ret 

    def get_depends_on(self):
        """
        Get a list of depends this job has on others

        :returns: list[:class:`.Depend`]
        """
        cdef list ret = get_job_depends_on(self)
        return ret

    def kill_tasks(self, object callback=None):
        """
        Kill all tasks on a job

        :param callback: Optional callback function to run if tasks were killed
        """
        tasks = get_tasks(job=self)
        if tasks:
            LOGGER.debug("Killing %d tasks", len(tasks))
            kill_tasks(tasks=tasks)
            if callback:
                callback()

    def eat_dead_tasks(self, object callback=None):
        """
        Eat all dead tasks on a job

        :param callback: Optional callback function to run if tasks were eaten
        """
        cdef list dead = self.get_tasks(states=[TaskState.DEAD])

        if dead:
            LOGGER.debug("Eating %d tasks", len(dead))
            eat_tasks(tasks=dead)
            if callback:
                callback()

    def retry_dead_tasks(self, object callback=None):
        """
        Retry all dead tasks on a job

        :param callback: Optional callback function to run if tasks were retried
        """
        cdef list dead = self.get_tasks(states=[TaskState.DEAD])

        if dead:
            LOGGER.debug("Retrying %d tasks", len(dead))
            retry_tasks(tasks=dead)
            if callback:
                callback()

    def get_procs(self):
        """
        Get current procs 

        :returns: list[:class:`.Proc`]
        """
        return get_procs(jobIds=[self.id])


def launch_job(JobSpec spec):
    """
    Launch a job with a JobSpec.

    :param spec: :class:`.JobSpec`
    :returns: :class:`.Job`
    """
    cdef Job job = spec.launch()
    return job

@reconnecting
def get_job(Guid& id):
    """
    Get a :class:`.Job`

    :param id: str Job id
    :returns: :class:`.Job`
    """
    cdef JobT jobT
    cdef Job job

    conn().proxy().getJob(jobT, id)

    job = initJob(jobT)
    return job

@reconnecting
def get_active_job(string name):
    """
    Get an active :class:`.Job`

    :param id: str name
    :returns: :class:`.Job`
    """    
    cdef JobT jobT
    cdef Job job

    conn().proxy().getActiveJob(jobT, name)

    job = initJob(jobT)
    return job

@reconnecting
def get_jobs(**kwargs):
    """
    Get a list of jobs matching a criteria.

    :param matchingOnly: bool
    :param regex: str regex pattern to match against job names
    :param project: list[str] of matching project codes
    :param user: list[str] of matching user names
    :param jobIds: list[str] of matching job ids
    :param name: list[str] of matching job names
    :param states: list[:obj:`.JobState`] 

    :returns: list[:class:`.Job`]
    """
    cdef: 
        vector[JobT] jobs 
        JobT jobT
        list ret 
        JobFilter filter = JobFilter(**kwargs)
        JobFilterT f = filter.value

    conn().proxy().getJobs(jobs, f)
    ret = [initJob(jobT) for jobT in jobs]
    return ret

@reconnecting
def kill_job(Job job, string reason):
    """
    Kill a job

    :param job: :class:`.Job`
    :param reason: str reason for killing the job 
    """
    conn().proxy().killJob(job.id, reason)
    job.refresh()

@reconnecting
def pause_job(Job job, bint paused):
    """
    Set the pause state of a job

    :param job: :class:`.Job`
    :param paused: bool pause state
    """
    conn().proxy().pauseJob(job.id, paused)
    job.refresh()

@reconnecting
def set_job_min_cores(Job job, int value):
    """
    Set the minimum number of cores a job should get 

    :param job: :class:`.Job`
    :param value: int number of cores
    """
    conn().proxy().setJobMinCores(job.id, value)
    job.refresh()

@reconnecting
def set_job_max_cores(Job job, int value):
    """
    Set the maximum number of cores a job should get 

    :param job: :class:`.Job`
    :param value: int number of cores
    """
    conn().proxy().setJobMaxCores(job.id, value)
    job.refresh()

@reconnecting
def get_job_outputs(object job):
    """
    Get the outputs of all layers of a :class:`.Job`

    :param job: :class:`.Job` or str id
    :returns: list[:class:`.Output`]
    """
    cdef:
        OutputT outT
        vector[OutputT] outputs
        Guid jobId
        list ret

    if isinstance(job, Job):
        jobId = job.id
    else:
        jobId = job

    conn().proxy().getJobOutputs(outputs, jobId)

    ret = [initOutput(outT) for outT in outputs]
    return ret


